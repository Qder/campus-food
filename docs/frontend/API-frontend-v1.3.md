# 校园美食智能推荐与社交平台 · HTTP 接口文档（前端版）

> 版本：V1.3　　对应说明书：详细设计说明书 V1.3　　面向读者：前端开发
> 本文档只描述**浏览器可直接调用的对外接口**。前端不直连任何后端进程，所有请求统一走 API Gateway。

---

## 1 概述

### 1.1 Base URL

| 环境 | Base URL |
| --- | --- |
| 本地开发 | `http://localhost:8080` |
| 测试 / 生产 | `https://<域名>`（HTTPS） |

所有路径均为**相对 Base URL** 的路径，例如 `GET /food/list` 实际请求 `http://localhost:8080/food/list`。

网关按第一段路径转发，前端只需关心下表的前缀，**不要**在代码里出现任何服务端口（8081–8085）。

| 前缀 | 覆盖能力 |
| --- | --- |
| `/auth/**`、`/user/**` | 注册登录、个人资料、收藏、提醒、动态、站内信、文件上传、公共公告读取 |
| `/food/**` | 分类、列表、搜索、详情、轮播图、推荐、评价与点赞回复 |
| `/social/**` | 好友、拉黑、聊天、拼单约饭 |
| `/merchant/**` | 商家入驻、店铺、菜品、营销活动、评价回复、经营统计 |
| `/order/**` | 下单、支付、订单查询与状态流转（独立订单能力，**与 `/merchant` 无关**） |
| `/admin/**` | 平台管理：用户与商家治理、内容审核、公告、推荐位、数据统计 |

> 商家接单、取消订单、完成订单也走 `/order/**`，**不要**调 `/merchant/**`。

### 1.2 鉴权

- 登录成功后服务端返回 JWT，前端保存在本地（推荐 `localStorage`），之后每个请求带：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.xxxxx
```

- 无 Token、Token 非法、Token 过期，统一返回 `40100`。前端收到 `40100` 后：清本地 Token → 跳登录页 → 登录成功后回跳到原页面（把当前路由写进 `redirect` 参数）。
- 未带 Token 只能访问：注册、登录、发送验证码、重置密码、分类、美食列表/搜索/详情/轮播图、评价列表、公告、店铺公开信息、推荐（降级热门）。
- 角色由登录返回的 `role` 决定（`USER` / `MERCHANT` / `ADMIN`），服务端同时返回数值 `roleId`（1/2/3）。前端按 `role` 决定进入哪个端，不要自己推断权限。
- 角色不符返回 `40300`，前端提示「当前账号无权访问该功能」，并回到本端首页。

### 1.3 角色与「未登录」的行为边界

| 场景 | 未登录 | USER | MERCHANT | ADMIN |
| --- | --- | --- | --- | --- |
| 浏览 / 搜索 / 详情 / 评价列表 | ✅ | ✅ | ✅ | ✅ |
| 个性化推荐 | 返回热门（不报错） | ✅ | ✅ | ✅ |
| 提交评价、收藏、下单、社交、拼单 | ❌ 40100 | ✅ | ✅ | ✅ |
| 商家后台（菜品 / 营销 / 评价回复） | ❌ | ❌ 40300 | ✅ | ✅ |
| 平台管理 `/admin/**` | ❌ | ❌ 40300 | ❌ 40300 | ✅ |

### 1.4 跨域

网关已开启 CORS，允许前端调试域名携带 `Authorization` 头。前端 `credentials` 不需要带 Cookie（本平台用 JWT，不用 Session）。若在本地用 Vite 代理，把 `/auth`、`/user`、`/food`、`/social`、`/merchant`、`/order`、`/admin` 一并代理到 `http://localhost:8080`。

### 1.5 日期与金额

- 时间：ISO-8601 带时区，如 `2026-10-26T21:08:00+08:00`。前端展示时统一用 `dayjs` 转 `YYYY-MM-DD HH:mm`。
- 日期：`YYYY-MM-DD`。
- 金额：单位**元**，服务端返回字符串或两位小数数字（如 `"28.50"` / `28.50`）。前端用 `toFixed(2)` 展示，**不要**用浮点数做累加比较。
- ID：`number`（对应数据库 `bigint unsigned` 主键）。`bigint` 精度上限问题本平台不会触发，但请勿对 ID 做算术运算。

### 1.6 接口实现进度（联调用）

| 能力 | 状态 |
| --- | --- |
| `/auth` 注册登录、`/user/profile`、`/food` 全部、`/merchant` 店铺与菜品、`/admin/merchants` | 已有实现，可直接联调 |
| `/social` 好友申请 / 好友列表 / 收件箱 / 删除好友 | 已有实现，可直接联调 |
| `/user` 收藏·提醒·动态·站内信·上传、`/social` 拉黑·聊天·拼单、`/order` 全部、`/admin` 治理与统计、`/merchant` 营销与评价回复 | 本文档定义契约，**按此先行开发**；未上线时网关返回 `40400`，前端按「功能即将开放」兜底 |

---

## 2 通用约定

### 2.1 统一响应体

**成功**（HTTP 200）：

```json
{ "code": 0, "message": "ok", "data": {} }
```

**失败**（HTTP 200，业务错误一律用 `code` 表达，不要只看 HTTP 状态码）：

```json
{ "code": 40900, "message": "你已报名该活动", "data": null }
```

- `message` 是**可直接展示给用户的中文**，前端不要自己拼错误文案，也不要按 `message` 做逻辑判断，判断用 `code`。
- `data` 失败时恒为 `null`；无返回内容的成功接口 `data` 为 `null`。
- 只有 `50000` 需要前端替换文案为「服务繁忙，请稍后重试」。

### 2.2 分页

列表接口统一返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [],
    "total": 0,
    "page": 1,
    "size": 10
  }
}
```

- `page` 从 **1** 开始；`size` 默认 10，最大 50，超过 50 按 50 处理。
- `total` 为总条数，前端据此算总页数：`Math.ceil(total / size)`。
- 空结果返回 `list: []`、`total: 0`，**不是** `40400`。

### 2.3 文件上传

统一一个上传接口，头像、评价图、菜品封面、店铺 Logo、营业执照都复用它（需登录，任意角色）。

```
POST /user/upload
Content-Type: multipart/form-data
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | jpg / jpeg / png / webp；单张 ≤ 5MB |
| bizType | string | 是 | `AVATAR` / `REVIEW` / `FOOD` / `MERCHANT_LOGO` / `LICENSE` |

成功 `data`：

```json
{ "url": "https://cdn.example.com/2026/10/abc.jpg", "size": 184320 }
```

失败示例：

```json
{ "code": 40001, "message": "图片不能超过 5MB", "data": null }
```

前端注意：上传成功后拿到 `url` 再调保存接口（如 `PUT /user/profile` 传 `avatarUrl`）；上传中按钮置灰并显示进度，失败提示「图片上传失败，请重试」，不要清空用户已填表单。

### 2.4 统一错误码

| code | 含义 | 前端处理 |
| --- | --- | --- |
| 0 | 成功 | 按接口说明渲染 |
| 40001 | 参数不合法 | `message` 已写明字段，直接 toast 或标红对应输入框 |
| 40100 | 未登录 / Token 无效或过期 | 清 Token，跳登录页，带 `redirect` |
| 40101 | 账号或密码错误 | 登录页表单顶部红字提示，保留账号输入 |
| 40102 | 账号已禁用 | 弹窗「账号已被禁用，如有疑问请联系平台客服」，停留登录页 |
| 40300 | 无权限（角色不符） | 提示无权限，回到本端首页 |
| 40301 | 已被对方拉黑 / 对方不允许陌生人添加 | 社交场景专用，toast `message` |
| 40400 | 资源不存在 | 展示空态或「内容已下架」 |
| 40900 | 业务冲突（已报名、已满、库存不足、重复下单等） | toast `message`，刷新当前数据 |
| 42900 | 验证码发送过于频繁 | 按钮进入 60s 倒计时，提示「请稍后再试」 |
| 50000 | 服务器错误 | 只提示「服务繁忙，请稍后重试」，可给「重试」按钮 |

### 2.5 通用请求头

| Header | 何时必填 | 说明 |
| --- | --- | --- |
| `Authorization: Bearer <token>` | 需登录接口 | 见 1.2 |
| `Content-Type: application/json` | JSON body 接口 | — |
| `Content-Type: multipart/form-data` | 上传接口 | 不要手动设 boundary |
| `Accept-Language` | 可选 | 本期固定中文，可不传 |

### 2.6 状态枚举与展示文案

前端不要在多个页面各写一套文案，建议放在 `constants.ts` 统一导出。

| 枚举 | 取值 → 展示文案 |
| --- | --- |
| 账号状态 `status` | `0` 禁用 / `1` 正常 |
| 评价审核 `auditStatus` | `0` 待审核 / `1` 通过 / `2` 未通过 |
| 评价展示 `status` | `1` 展示中 / `0` 已隐藏 |
| 商家审核 `auditStatus` | `0` 待审核 / `1` 已通过 / `2` 已驳回 / `3` 已取消资格 |
| 商家经营 `status` | `0` 未开业 / `1` 营业中 / `2` 停业 |
| 菜品 `status` | `0` 草稿 / `1` 已上架 / `2` 已下架 |
| 拼单活动 `status` | `0` 报名中 / `1` 已满 / `2` 进行中 / `3` 已结束 / `4` 已取消 |
| 拼单成员 `status` | `1` 已报名 / `2` 已退出 / `3` 已被移除 |
| 好友申请 `status` | `0` 待处理 / `1` 已同意 / `2` 已拒绝 / `3` 已过期 |
| 订单 `status` | `PENDING_PAY` 待支付 / `PAID` 已支付待接单 / `ACCEPTED` 已接单 / `COMPLETED` 已完成 / `CANCELLED` 已取消 |
| 配送方式 `deliveryType` | `1` 到店自提 / `2` 配送到寝 |
| 支付渠道 `payChannel` | `MOCK` 模拟支付 / `WECHAT` 微信 / `ALIPAY` 支付宝 |
| 支付状态 `status` | `0` 待支付 / `1` 支付成功 / `2` 支付失败 / `3` 已退款 |
| 处罚类型 `action` | `WARN` 警告 / `BAN` 封禁（**本期没有「限制」**） |
| 营销活动 `campaignType` | `DISCOUNT` 折扣 / `FULL_REDUCE` 满减 / `GROUP_BUY` 团购 |
| 营销活动 `status` | `0` 草稿 / `1` 进行中 / `2` 已结束 / `3` 已下线 |
| 消息类型 `msgType` | `TEXT` 文字 / `IMAGE` 图片 / `EMOJI` 表情 |
| 审核单 `contentType` | `REVIEW` 评价 / `REPLY` 回复 / `FEED` 动态 / `MERCHANT_INFO` 店铺资料 / `IMAGE` 图片 |

---
## 3 接口分组

> 分组按**页面/场景**，与说明书第 4 章模块一一对应。每组的编号即说明书小节号。

---

## 4.1 登录注册

对应页面：登录页、注册页、忘记密码页。流程：判断有无账号 → 注册（格式与唯一性校验 → 验证码 → 写入用户 → 签发 JWT）/ 登录（账号密码 → 忘记密码可重置 → 校验账号状态 → 签发 JWT）→ 按 `role` 进入用户端 / 商家端 / 管理端。

### 4.1.1 发送验证码

**前端使用场景**：注册页、忘记密码页点击「获取验证码」按钮；按钮点击后进入 60 秒倒计时。

`POST /auth/code`

**是否需要登录**：否　**允许角色**：全部

**Header**：`Content-Type: application/json`

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| target | string | 是 | `13800138000` | 手机号 11 位，或邮箱 |
| targetType | string | 是 | `PHONE` | `PHONE` / `EMAIL` |
| scene | string | 是 | `REGISTER` | `REGISTER` 注册 / `RESET` 重置密码 |

**成功响应 data**

```json
{ "expireIn": 300, "resendAfter": 60 }
```

> `expireIn` 验证码有效期（秒），`resendAfter` 可重发间隔（秒），前端直接用它做倒计时。

**失败示例**

```json
{ "code": 40001, "message": "手机号格式不正确", "data": null }
```

```json
{ "code": 42900, "message": "验证码发送过于频繁，请 60 秒后再试", "data": null }
```

**前端注意**：倒计时期间按钮置灰且显示「重新获取(59s)」；`42900` 时也要进入倒计时，避免用户狂点。

---

### 4.1.2 注册

**前端使用场景**：注册页点击「注册并登录」。

`POST /auth/register`

**是否需要登录**：否　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| phone | string | 二选一 | `13800138000` | 与 `email` 至少填一个；`^1[3-9]\d{9}$` |
| email | string | 二选一 | `a@b.com` | 与 `phone` 至少填一个；邮箱格式 |
| password | string | 是 | `abc12345` | 6–32 位，不含空格 |
| nickname | string | 是 | `小杨` | 1–20 字 |
| code | string | 是 | `864213` | 6 位数字，对应 `scene=REGISTER` 的验证码 |

**成功响应 data**（注册后自动登录，直接可跳转）

```json
{
  "id": 1001,
  "nickname": "小杨",
  "role": "USER",
  "roleId": 1,
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAxIn0.xxxxx",
  "expiresIn": 604800
}
```

**失败示例**

```json
{ "code": 40001, "message": "密码长度必须在 6 到 32 个字符之间", "data": null }
```

```json
{ "code": 40001, "message": "手机号已被注册", "data": null }
```

```json
{ "code": 40900, "message": "验证码错误或已过期", "data": null }
```

**前端注意**：校验失败的 `message` 已指明字段，直接标红对应输入框；注册成功立即写入 Token 并跳首页，不要再让用户登录一次。

---

### 4.1.3 登录

**前端使用场景**：登录页点击「登录」。

`POST /auth/login`

**是否需要登录**：否　**允许角色**：全部（三种角色同一入口）

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| username | string | 是 | `13800138000` 或 `a@b.com` | 手机号或邮箱，非空 |
| password | string | 是 | `abc12345` | 非空 |

**成功响应 data**

```json
{
  "id": 1001,
  "nickname": "小杨",
  "role": "USER",
  "roleId": 1,
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAxIn0.xxxxx",
  "expiresIn": 604800
}
```

**失败示例**

```json
{ "code": 40101, "message": "账号或密码错误", "data": null }
```

```json
{ "code": 40102, "message": "账号已被禁用", "data": null }
```

```json
{ "code": 40001, "message": "账号不能为空", "data": null }
```

**前端注意**

- `40101`：只提示错误，**不要**清空已输入的账号；连续 3 次失败后高亮「忘记密码」入口。
- `40102`：弹窗提示后停留在登录页，不写入 Token。
- 成功后按 `role` 路由：`USER` → 用户端首页，`MERCHANT` → 商家后台首页，`ADMIN` → 管理后台首页；若 `role=MERCHANT` 但店铺 `auditStatus!=1`，进后台先落到「入驻进度」页。
- 登录按钮在请求期间 loading 且禁用，防止重复提交。

---

### 4.1.4 重置密码（忘记密码）

**前端使用场景**：登录页点「忘记密码」→ 填手机号/邮箱 → 获取验证码 → 设新密码。

`POST /auth/password/reset`

**是否需要登录**：否　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| target | string | 是 | `13800138000` | 手机号或邮箱，须已注册 |
| targetType | string | 是 | `PHONE` | `PHONE` / `EMAIL` |
| code | string | 是 | `864213` | 对应 `scene=RESET` 的验证码 |
| newPassword | string | 是 | `new12345` | 6–32 位，不含空格 |

**成功响应 data**

```json
null
```

**失败示例**

```json
{ "code": 40900, "message": "验证码错误或已过期", "data": null }
```

```json
{ "code": 40400, "message": "该账号未注册", "data": null }
```

**前端注意**：重置成功后回到登录页并预填账号，提示「密码已重置，请重新登录」，不要自动登录。

---

### 4.1.5 退出登录

**前端使用场景**：任意页面点击头像菜单「退出登录」。

`POST /auth/logout`

**是否需要登录**：是　**允许角色**：全部

**Header**：`Authorization: Bearer <token>`

**Body**：无

**成功响应 data**

```json
null
```

**失败示例**

```json
{ "code": 40100, "message": "登录已过期，请重新登录", "data": null }
```

**前端注意**：无论接口是否成功，前端都清本地 Token 并跳登录页；`40100` 属于正常情况，不要弹错误框。

---

## 4.2 浏览与搜索

对应页面：首页、分类页、搜索结果页、美食详情页。未登录**可以**浏览列表与详情；登录后才会记录浏览行为、并为推荐模块提供数据。

### 4.2.1 获取美食分类

**前端使用场景**：首页/分类页顶部分类导航、筛选抽屉。

`GET /food/categories`

**是否需要登录**：否　**允许角色**：全部

**Query**：无

**成功响应 data**

```json
[
  {
    "id": 1,
    "parentId": 0,
    "name": "中餐",
    "sortNo": 1,
    "children": [
      { "id": 11, "parentId": 1, "name": "川菜", "sortNo": 1, "children": [] }
    ]
  },
  {
    "id": 2,
    "parentId": 0,
    "name": "小吃",
    "sortNo": 2,
    "children": []
  }
]
```

> `id ← food_category.id`，`parentId ← food_category.parent_id`，只返回 `status=1`（启用）的分类。

**失败示例**

```json
{ "code": 50000, "message": "服务繁忙，请稍后重试", "data": null }
```

**前端注意**：分类树建议首次进入缓存到 `sessionStorage`，本会话内不重复拉取；为空时隐藏分类栏而不是报错。

---

### 4.2.2 美食列表（分类 / 区域浏览）

**前端使用场景**：首页「推荐/全部」信息流、分类页、区域切换、下拉加载更多。

`GET /food/list`

**是否需要登录**：否（带 Token 时会额外记录浏览行为）　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| categoryId | number | 否 | `11` | 分类 ID ← `food_category.id` |
| campusArea | string | 否 | `一食堂` | 校园区域 ← `merchant.campus_area` |
| keyword | string | 否 | `麻辣` | 可选模糊匹配名称（与搜索页共用） |
| sort | string | 否 | `default` | `default` 综合 / `priceAsc` 价格升 / `priceDesc` 价格降 / `salesDesc` 销量 |
| page | number | 否 | `1` | 从 1 开始 |
| size | number | 否 | `10` | 最大 50 |

**成功响应 data**

```json
{
  "list": [
    {
      "foodId": 501,
      "name": "麻婆豆腐",
      "merchantId": 21,
      "shopName": "川味小馆",
      "campusArea": "一食堂",
      "categoryId": 11,
      "foodType": "川菜",
      "price": "12.00",
      "stock": 38,
      "coverUrl": "https://cdn.example.com/food/501.jpg",
      "avgScore": 4.70,
      "reviewCount": 26,
      "salesCount": 412,
      "recommendTag": "招牌",
      "status": 1
    }
  ],
  "total": 128,
  "page": 1,
  "size": 10
}
```

> `foodId ← food_item.id`、`shopName ← merchant.shop_name`、`avgScore ← food_item.avg_score`、`status ← food_item.status`（1 已上架）。

**失败示例**

```json
{ "code": 40001, "message": "size 不能超过 50", "data": null }
```

**前端注意**

- 只返回 `status=1`（上架）的菜品；空结果显示「暂无美食」插画 + 「换个分类看看」。
- 触底加载：用 `total` 判断是否还有下一页，`list.length >= total` 时不再请求；加载中显示骨架屏。
- `stock=0` 的卡片打「已售罄」角标且不可点击下单，但**仍可进详情**。

---

### 4.2.3 美食搜索

**前端使用场景**：顶栏搜索框回车、搜索页、搜索历史点击。

`GET /food/search`

**是否需要登录**：否　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| keyword | string | 是 | `麻婆` | 去空白后不能为空，否则 `40001`；匹配美食名称与店铺名称 |
| sort | string | 否 | `default` | 同 4.2.2 |
| page | number | 否 | `1` | 从 1 开始 |
| size | number | 否 | `10` | 最大 50 |

**成功响应 data**：结构同 4.2.2。

**失败示例**

```json
{ "code": 40001, "message": "搜索关键词不能为空", "data": null }
```

**前端注意**：空关键词不要发请求，直接提示；搜索建议做 300ms 防抖；无结果时展示「没有找到「麻婆」相关美食」，并给「看看热门」按钮跳 4.2.2。

---

### 4.2.4 美食详情

**前端使用场景**：列表/搜索结果点击卡片进入详情页。

`GET /food/{foodId}`

**是否需要登录**：否（登录后记录浏览，用于推荐）　**允许角色**：全部

**Path**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| foodId | number | 是 | `501` | ← `food_item.id` |

**成功响应 data**

```json
{
  "foodId": 501,
  "name": "麻婆豆腐",
  "merchantId": 21,
  "shopName": "川味小馆",
  "campusArea": "一食堂",
  "shopAddress": "一食堂二楼 12 号窗口",
  "shopPhone": "13800138001",
  "categoryId": 11,
  "foodType": "川菜",
  "price": "12.00",
  "stock": 38,
  "description": "地道川味，微辣。",
  "coverUrl": "https://cdn.example.com/food/501.jpg",
  "images": [
    "https://cdn.example.com/food/501-1.jpg",
    "https://cdn.example.com/food/501-2.jpg"
  ],
  "avgScore": 4.70,
  "reviewCount": 26,
  "salesCount": 412,
  "recommendTag": "招牌",
  "tasteTags": "辣,麻",
  "ingredients": "豆腐,牛肉末,豆瓣酱",
  "status": 1,
  "isFavorited": false
}
```

> `images ← food_image.url`（按 `sort_no` 升序）；`isFavorited` 未登录恒为 `false`。

**失败示例**

```json
{ "code": 40400, "message": "该美食已下架", "data": null }
```

**前端注意**：未登录点「收藏」先弹登录；`stock=0` 时「加入购物车/立即下单」按钮置灰并显示「已售罄」；详情页浏览不弹任何登录墙。

---

### 4.2.5 店铺公开信息

**前端使用场景**：详情页点店铺名进入店铺页。

`GET /merchant/public/{merchantId}`

**是否需要登录**：否　**允许角色**：全部

**Path**：`merchantId` ← `merchant.id`

**成功响应 data**

```json
{
  "merchantId": 21,
  "shopName": "川味小馆",
  "campusArea": "一食堂",
  "address": "一食堂二楼 12 号窗口",
  "contactPhone": "13800138001",
  "logoUrl": "https://cdn.example.com/shop/21.png",
  "description": "主打川菜，现炒现做。",
  "auditStatus": 1,
  "status": 1
}
```

**失败示例**

```json
{ "code": 40400, "message": "店铺不存在", "data": null }
```

**前端注意**：只展示 `auditStatus=1` 且 `status!=2` 的店铺；停业时页面顶部显示「本店暂停营业」。

---

### 4.2.6 平台公告列表

**前端使用场景**：首页公告条、公告页。

`GET /user/notices`

**是否需要登录**：否　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| page | number | 否 | `1` | — |
| size | number | 否 | `10` | 最大 50 |

**成功响应 data**

```json
{
  "list": [
    {
      "noticeId": 3,
      "title": "国庆期间营业时间调整",
      "content": "10 月 1 日至 3 日一食堂营业时间调整为 10:00—19:00。",
      "publishedAt": "2026-09-28T10:00:00+08:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 10
}
```

**失败示例**

```json
{ "code": 40001, "message": "size 不能超过 50", "data": null }
```

**前端注意**：只返回已发布公告（`publishStatus=1`）；空时隐藏首页公告条。

---

## 4.3 个性化推荐

对应页面：首页「猜你喜欢」、推荐页。**未登录或 Token 无效时返回热门列表，不要报 401 打断首页。**

### 4.3.1 获取推荐列表

**前端使用场景**：首页首屏「猜你喜欢」模块、推荐页下拉刷新。

`GET /food/recommend`

**是否需要登录**：否（未登录/Token 无效时降级为热门）　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| limit | number | 否 | `10` | 默认 10，最大 50 |
| scene | string | 否 | `HOME` | `HOME` 首页 / `RECOMMEND` 推荐页 |

**成功响应 data**

```json
{
  "degraded": true,
  "reason": "NOT_LOGIN",
  "list": [
    {
      "foodId": 501,
      "name": "麻婆豆腐",
      "shopName": "川味小馆",
      "price": "12.00",
      "coverUrl": "https://cdn.example.com/food/501.jpg",
      "avgScore": 4.70,
      "salesCount": 412,
      "recommendTag": "招牌",
      "score": 0.9231,
      "recommendTags": "辣,麻"
    }
  ]
}
```

- `degraded`：`true` 表示本次是降级结果（未登录、未填口味或模型不可用）。
- `reason`：`NOT_LOGIN` 未登录 / `NO_TASTE_TAGS` 未填口味标签 / `MODEL_FALLBACK` 模型降级；`degraded=false` 时为 `null`。
- `score`：推荐打分 0–1，前端只用于排序展示，不必显示。

**失败示例**（仅在服务异常时出现，前端按空态处理）

```json
{ "code": 50000, "message": "服务繁忙，请稍后重试", "data": null }
```

**前端注意**

- **绝对不要**因为推荐接口返回 `40100` 就把用户踢去登录页；本接口设计上不返回 `40100`。若真收到 `40100`，也按「热门兜底」渲染。
- `degraded=true` 时模块标题显示「热门推荐」而不是「猜你喜欢」，避免用户误解。
- 若 `reason=NO_TASTE_TAGS`，展示轻提示「完善口味偏好，推荐更准」→ 跳 4.5.2。
- 列表为空时整块隐藏，不要留白。

---

## 4.4 评价与分享

对应页面：美食详情页评价区、写评价弹窗、我的评价、商家评价回复（见 4.9.8）。**必须登录**才能提交评价。

### 4.4.1 评价列表

**前端使用场景**：详情页「评价」Tab 首屏与翻页。

`GET /food/{foodId}/reviews`

**是否需要登录**：否　**允许角色**：全部

**Path**：`foodId` ← `food_item.id`

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| page | number | 否 | `1` | — |
| size | number | 否 | `10` | 最大 50 |
| scoreFilter | number | 否 | `5` | 只看某星级，1–5，不传为全部 |
| withImage | boolean | 否 | `true` | 只看有图评价 |

**成功响应 data**

```json
{
  "list": [
    {
      "reviewId": 9001,
      "userId": 1001,
      "nickname": "小杨",
      "avatarUrl": "https://cdn.example.com/avatar/1001.png",
      "score": 5,
      "content": "分量足，微辣刚好。",
      "images": ["https://cdn.example.com/review/9001-1.jpg"],
      "likeCount": 12,
      "liked": false,
      "replyCount": 1,
      "createdAt": "2026-10-20T12:30:00+08:00"
    }
  ],
  "total": 26,
  "page": 1,
  "size": 10
}
```

> `reviewId ← food_review.id`，`likeCount ← food_review.like_count`，`images ← review_image.url`。只返回 `status=1`（展示）且 `auditStatus=1`（审核通过）的评价。

**失败示例**

```json
{ "code": 40400, "message": "该美食不存在", "data": null }
```

**前端注意**：评价为空显示「还没有评价，来说点什么」；`liked` 用来决定心形图标是否实心；点赞不要整页刷新，只更新 `likeCount`。

---

### 4.4.2 提交评价

**前端使用场景**：详情页「写评价」→ 选星 → 填文字、传图 → 点「提交」。

`POST /food/{foodId}/reviews`

**是否需要登录**：是　**允许角色**：USER（商家/管理员不参与用户评价）

**Header**：`Authorization: Bearer <token>`

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| score | number | 是 | `5` | 1–5 整数 |
| content | string | 否 | `分量足，微辣刚好。` | 最长 2000 字 |
| images | string[] | 否 | `["https://cdn.../1.jpg"]` | 最多 9 张，须先用 2.3 上传 |
| orderId | number | 否 | `7001` | 关联订单，← `food_order.id` |
| isAnonymous | boolean | 否 | `false` | 是否匿名展示 |

**成功响应 data**

```json
{
  "reviewId": 9001,
  "auditStatus": 0,
  "status": 1,
  "message": "已提交，审核后展示"
}
```

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

```json
{ "code": 40001, "message": "评分需在 1~5 之间", "data": null }
```

```json
{ "code": 40900, "message": "你已评价过该美食", "data": null }
```

```json
{ "code": 40900, "message": "该美食已下架，无法评价", "data": null }
```

**前端注意**

- 提交成功**不要**把评价直接插到列表里（还在待审核），而是提示「已提交，审核后展示」并清空表单、关闭弹窗。审核通过与拒绝可通过 4.5.9 站内信获知。
- 未选星级时「提交」按钮置灰；上传图片时显示本地缩略图，失败可单张重试。
- `40900 已评价过` 时把按钮文案改成「已评价」并禁用。

---

### 4.4.3 点赞 / 取消点赞

**前端使用场景**：评价卡片右下角心形图标。

`POST /food/reviews/{reviewId}/like`（点赞）　`DELETE /food/reviews/{reviewId}/like`（取消）

**是否需要登录**：是　**允许角色**：全部（含商家）

**Path**：`reviewId` ← `food_review.id`

**成功响应 data**

```json
{ "reviewId": 9001, "liked": true, "likeCount": 13 }
```

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

```json
{ "code": 40400, "message": "评价不存在或已隐藏", "data": null }
```

**前端注意**：做**乐观更新**（先改 UI 再发请求），失败回滚并 toast；不要为此整页刷新。未登录点击时跳登录页。

---

### 4.4.4 评价回复列表 / 提交回复

**前端使用场景**：点评价「查看回复」展开；用户或商家在回复框输入后发送。

`GET /food/reviews/{reviewId}/replies`

**是否需要登录**：否　**允许角色**：全部

**成功响应 data**

```json
[
  {
    "replyId": 3001,
    "reviewId": 9001,
    "fromUserId": 21,
    "fromNickname": "川味小馆",
    "fromRole": "MERCHANT",
    "toNickname": null,
    "content": "感谢支持，欢迎下次再来！",
    "createdAt": "2026-10-20T13:00:00+08:00"
  }
]
```

`POST /food/reviews/{reviewId}/replies`

**是否需要登录**：是　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| content | string | 是 | `感谢支持！` | 1–500 字 |
| toReplyId | number | 否 | `3001` | 回复某条回复时传 ← `review_reply.id` |

**成功响应 data**：结构同上单条。

**失败示例**

```json
{ "code": 40001, "message": "回复内容不能为空", "data": null }
```

**前端注意**：回复后只更新当前评价的 `replyCount` 并展开列表；商家身份时回复框预填「商家回复」标识。

---

### 4.4.5 生成分享链接

**前端使用场景**：详情页点「分享」时，先取分享信息再调起微信/QQ/微博分享或复制链接。

`GET /food/{foodId}/share`

**是否需要登录**：否　**允许角色**：全部

**成功响应 data**

```json
{
  "title": "麻婆豆腐 · 川味小馆",
  "desc": "一食堂二楼，4.7 分，￥12.00",
  "url": "https://m.campusfood.example.com/food/501",
  "coverUrl": "https://cdn.example.com/food/501.jpg"
}
```

**失败示例**

```json
{ "code": 40400, "message": "该美食已下架", "data": null }
```

**前端注意**：分享是纯前端行为，接口只提供素材；不支持的目标平台（如未装微信）给出「链接已复制」兜底。
---

## 4.5 用户中心

对应页面：个人中心、资料编辑、隐私设置、收藏、提醒、我的订单（跳 4.8）、我的动态、站内信。

### 4.5.1 获取当前登录用户资料

**前端使用场景**：进入任意页面时拉一次「我」的信息（头像、昵称、角色）；个人中心首屏。

`GET /user/profile`

**是否需要登录**：是　**允许角色**：全部

**Header**：`Authorization: Bearer <token>`

**成功响应 data**

```json
{
  "userId": 1001,
  "phone": "138****8000",
  "email": "a***@b.com",
  "nickname": "小杨",
  "avatarUrl": "https://cdn.example.com/avatar/1001.png",
  "gender": 1,
  "school": "示例大学",
  "major": "计算机科学与技术",
  "bio": "爱吃饭，爱拼单。",
  "privacyFeed": 1,
  "privacyAddFriend": 1,
  "role": "USER",
  "roleId": 1,
  "status": 1,
  "tasteTags": "辣,麻",
  "dislikeIngredients": "香菜",
  "createdAt": "2026-09-01T09:00:00+08:00"
}
```

> `userId ← sys_user.id`，`avatarUrl ← sys_user.avatar_url`，`privacyFeed`/`privacyAddFriend` 取 0/1。`status` 只有 `0` 禁用、`1` 正常（**没有「限制」状态**）。

**失败示例**

```json
{ "code": 40100, "message": "登录已过期，请重新登录", "data": null }
```

**前端注意**：手机号与邮箱做脱敏展示；本接口的 `status` 若为 `0`，说明账号刚被处罚，前端提示后跳登录页。

---

### 4.5.2 修改个人资料与隐私

**前端使用场景**：资料编辑页点「保存」；隐私开关切换后立即保存。

`PUT /user/profile`

**是否需要登录**：是　**允许角色**：全部

**Body**（**只传要改的字段**，未传的字段不变）

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| nickname | string | 否 | `小杨` | 1–20 字 |
| avatarUrl | string | 否 | `https://...` | ≤ 512 字符 |
| gender | number | 否 | `1` | `0` 未知 / `1` 男 / `2` 女 |
| school | string | 否 | `示例大学` | ≤ 128 字 |
| major | string | 否 | `计算机科学与技术` | ≤ 128 字 |
| bio | string | 否 | `爱吃饭` | ≤ 512 字 |
| tasteTags | string[] | 否 | `["辣","麻"]` | ≤ 10 个，用于推荐 |
| dislikeIngredients | string[] | 否 | `["香菜"]` | ≤ 10 个，忌口过滤 |
| privacyFeed | number | 否 | `1` | 0 不公开 / 1 公开 |
| privacyAddFriend | number | 否 | `1` | 0 不允许陌生人加 / 1 允许 |

**成功响应 data**：同 4.5.1 的完整资料对象。

**失败示例**

```json
{ "code": 40001, "message": "昵称长度不能超过 20 位", "data": null }
```

```json
{ "code": 40001, "message": "口味标签最多选择 10 个", "data": null }
```

**前端注意**：隐私开关建议**乐观更新**，失败回滚开关位置并 toast；口味标签改动后下次进首页会自动换成非降级推荐，无需手动刷新。

---

### 4.5.3 收藏列表

**前端使用场景**：个人中心「我的收藏」Tab，切换「美食 / 店铺」。

`GET /user/favorites`

**是否需要登录**：是　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| targetType | number | 否 | `1` | `1` 美食 / `2` 店铺，不传为全部 |
| page / size | number | 否 | `1` / `10` | size 最大 50 |

**成功响应 data**

```json
{
  "list": [
    {
      "favoriteId": 601,
      "targetType": 1,
      "targetId": 501,
      "title": "麻婆豆腐",
      "subTitle": "川味小馆 · 一食堂",
      "coverUrl": "https://cdn.example.com/food/501.jpg",
      "price": "12.00",
      "avgScore": 4.70,
      "status": 1,
      "createdAt": "2026-10-21T18:20:00+08:00"
    }
  ],
  "total": 8,
  "page": 1,
  "size": 10
}
```

> `targetType`/`targetId ← user_favorite.target_type / target_id`；`status` 为目标的当前状态（美食 1 上架、2 下架），下架时卡片置灰。

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：已下架或已删除的收藏要显示「已失效」并允许左滑取消收藏；空态给「去逛逛」按钮。

---

### 4.5.4 添加 / 取消收藏

**前端使用场景**：详情页「收藏」心形按钮、店铺页「收藏店铺」。

`POST /user/favorites`（添加）　`DELETE /user/favorites`（取消）

**是否需要登录**：是　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| targetType | number | 是 | `1` | `1` 美食 / `2` 店铺 |
| targetId | number | 是 | `501` | 美食或店铺 ID |

**成功响应 data**

```json
{ "targetType": 1, "targetId": 501, "favorited": true }
```

**失败示例**

```json
{ "code": 40900, "message": "已经收藏过了", "data": null }
```

```json
{ "code": 40400, "message": "该美食已下架", "data": null }
```

**前端注意**：乐观更新图标；`40900 已收藏` 视为成功态（把图标置为实心），不弹错误。

---

### 4.5.5 提醒设置

**前端使用场景**：个人中心「消息提醒」页的开关列表。

`GET /user/reminders`　`PUT /user/reminders`

**是否需要登录**：是　**允许角色**：全部

**GET 成功响应 data**

```json
[
  { "remindType": "CAMPAIGN", "label": "优惠活动", "channel": "INBOX", "enabled": true },
  { "remindType": "FRIEND_FEED", "label": "好友动态", "channel": "INBOX", "enabled": true },
  { "remindType": "ORDER", "label": "订单状态", "channel": "INBOX", "enabled": true }
]
```

**PUT Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| remindType | string | 是 | `ORDER` | `CAMPAIGN` / `FRIEND_FEED` / `ORDER` |
| channel | string | 否 | `INBOX` | 本期仅 `INBOX` 站内信 |
| enabled | boolean | 是 | `true` | — |

**PUT 成功响应 data**

```json
{ "remindType": "ORDER", "channel": "INBOX", "enabled": true }
```

**失败示例**

```json
{ "code": 40001, "message": "不支持的提醒类型", "data": null }
```

**前端注意**：开关即时保存，失败回滚；`ORDER` 提醒关闭后，订单状态变化仍可在订单页看到，只是不推送站内信。

---

### 4.5.6 我的动态

**前端使用场景**：个人中心「我的动态」Tab、发布动态。

`GET /user/feeds`（列表）　`POST /user/feeds`（发布）　`DELETE /user/feeds/{feedId}`（删除）

**是否需要登录**：是　**允许角色**：全部

**GET Query**：`page`、`size`

**GET 成功响应 data**

```json
{
  "list": [
    {
      "feedId": 801,
      "content": "今天食堂的麻婆豆腐不错",
      "images": ["https://cdn.example.com/feed/801-1.jpg"],
      "privacy": 1,
      "likeCount": 3,
      "commentCount": 0,
      "createdAt": "2026-10-24T12:10:00+08:00"
    }
  ],
  "total": 2,
  "page": 1,
  "size": 10
}
```

**POST Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| content | string | 是 | `今天食堂不错` | 1–1000 字 |
| images | string[] | 否 | `["https://..."]` | ≤ 9 张，先走 2.3 上传 |
| privacy | number | 否 | `1` | 0 仅自己可见 / 1 公开，默认 1 |

**POST 成功响应 data**

```json
{ "feedId": 802, "auditStatus": 0 }
```

**失败示例**

```json
{ "code": 40001, "message": "动态内容不能为空", "data": null }
```

**前端注意**：发布成功提示「已发布」；若被审核隐藏，列表中该条显示「审核中」灰标。删除需二次确认，成功后本地移除该条。

---

### 4.5.7 站内信 / 消息中心

**前端使用场景**：顶栏铃铛未读数、消息中心列表。

`GET /user/inbox`　`PUT /user/inbox/{messageId}/read`　`PUT /user/inbox/read-all`

**是否需要登录**：是　**允许角色**：全部

**GET Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| msgType | string | 否 | `ORDER` | `SYSTEM` / `CAMPAIGN` / `FRIEND` / `ORDER` |
| onlyUnread | boolean | 否 | `true` | 只看未读 |
| page / size | number | 否 | `1` / `10` | — |

**GET 成功响应 data**

```json
{
  "unreadTotal": 3,
  "list": [
    {
      "messageId": 1101,
      "msgType": "ORDER",
      "title": "订单已接单",
      "content": "川味小馆已接单，预计 20 分钟出餐。",
      "refType": "ORDER",
      "refId": 7001,
      "isRead": 0,
      "createdAt": "2026-10-26T12:05:00+08:00"
    }
  ],
  "total": 3,
  "page": 1,
  "size": 10
}
```

**标记已读成功响应 data**

```json
{ "messageId": 1101, "isRead": 1 }
```

**失败示例**

```json
{ "code": 40400, "message": "消息不存在", "data": null }
```

**前端注意**：铃铛未读数用 `unreadTotal`；点击消息后调已读接口再跳 `refType/refId` 对应页面（如 `ORDER` → 订单详情）；「全部已读」成功后把 `unreadTotal` 置 0。

---

### 4.5.8 上传文件

见 **2.3**（`POST /user/upload`），头像、评价图、菜品图、店铺 Logo、营业执照共用。

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

```json
{ "code": 40001, "message": "不支持的图片格式，请上传 jpg、png 或 webp", "data": null }
```

**前端注意**：上传前先在本地校验格式与大小，减少无效请求；`40100` 时跳登录页并保留用户已填表单。

---

## 4.6 社交互动

对应页面：好友列表、加好友、好友申请、聊天窗口、黑名单。入口是「选择操作」：添加好友 / 发起聊天 / 删除或拉黑。

### 4.6.1 搜索用户

**前端使用场景**：加好友页输入手机号/昵称搜索。

`GET /social/users/search`

**是否需要登录**：是　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| keyword | string | 是 | `小杨` 或 `13800138000` | 昵称模糊或手机号精确 |
| page / size | number | 否 | `1` / `10` | — |

**成功响应 data**

```json
{
  "list": [
    {
      "userId": 1002,
      "nickname": "小杨",
      "avatarUrl": "https://cdn.example.com/avatar/1002.png",
      "school": "示例大学",
      "relation": "STRANGER",
      "allowAddFriend": true
    }
  ],
  "total": 1,
  "page": 1,
  "size": 10
}
```

- `relation`：`SELF` 本人 / `FRIEND` 已是好友 / `REQUESTED` 已发申请 / `STRANGER` 陌生人 / `BLACKLISTED` 已拉黑。
- `allowAddFriend ← sys_user.privacy_add_friend`。

**失败示例**

```json
{ "code": 40001, "message": "搜索关键词不能为空", "data": null }
```

**前端注意**：`relation` 直接决定按钮文案：`SELF` 隐藏按钮、`FRIEND` 显示「发消息」、`REQUESTED` 显示「已申请」禁用、`STRANGER` 且 `allowAddFriend=false` 显示「对方未开放添加」禁用、`BLACKLISTED` 显示「已拉黑」不可加。

---

### 4.6.2 发送好友申请

**前端使用场景**：搜索结果或用户主页点「加好友」，可填验证留言。

`POST /social/friend-requests`

**是否需要登录**：是　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| toUserId | number | 是 | `1002` | ← `sys_user.id`，不能是本人 |
| message | string | 否 | `一起拼饭吗` | ≤ 255 字 |

**成功响应 data**

```json
{
  "requestId": 4001,
  "toUserId": 1002,
  "status": 0,
  "createdAt": "2026-10-26T20:00:00+08:00"
}
```

**失败示例**

```json
{ "code": 40301, "message": "对方不允许陌生人添加好友", "data": null }
```

```json
{ "code": 40900, "message": "你们已经是好友了", "data": null }
```

```json
{ "code": 40301, "message": "你已被对方拉黑，无法添加", "data": null }
```

```json
{ "code": 40900, "message": "已发送过申请，请等待对方处理", "data": null }
```

**前端注意**

- 三条业务分支必须分别提示：**本人/被拉黑 → 不可添加**、**已是好友 → 提示「你们已经是好友，去聊天」并给跳转按钮**、**陌生人看 `allowAddFriend`**。
- `40900 已是好友` 时按钮直接变「发消息」。
- 申请发出后按钮置为「已申请」并禁用，不要重复点击。

---

### 4.6.3 好友申请收件箱

**前端使用场景**：「新的朋友」页、顶栏红点。

`GET /social/friend-requests/inbox`

**是否需要登录**：是　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| status | number | 否 | `0` | `0` 待处理 / `1` 已同意 / `2` 已拒绝 / `3` 已过期 |
| page / size | number | 否 | `1` / `10` | — |

**成功响应 data**

```json
{
  "pendingTotal": 1,
  "list": [
    {
      "requestId": 4001,
      "fromUserId": 1002,
      "fromNickname": "小杨",
      "fromAvatarUrl": "https://cdn.example.com/avatar/1002.png",
      "message": "一起拼饭吗",
      "status": 0,
      "createdAt": "2026-10-26T20:00:00+08:00",
      "handledAt": null
    }
  ],
  "total": 1,
  "page": 1,
  "size": 10
}
```

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：`pendingTotal` 用于红点；「新的朋友」页**每 30 秒轮询**一次，或进入页面时刷新；`status=3 已过期` 的申请置灰显示「已过期」。

---

### 4.6.4 处理好友申请（同意 / 拒绝）

**前端使用场景**：申请卡片右侧「同意」「拒绝」按钮。

`POST /social/friend-requests/{requestId}/handle`

**是否需要登录**：是　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| action | string | 是 | `ACCEPT` | `ACCEPT` 同意 / `REJECT` 拒绝 |

**成功响应 data**

```json
{ "requestId": 4001, "status": 1, "friendUserId": 1002 }
```

**失败示例**

```json
{ "code": 40900, "message": "该申请已被处理", "data": null }
```

```json
{ "code": 40300, "message": "无权处理该申请", "data": null }
```

**前端注意**：同意后双向成为好友（服务端写双向关系），前端把卡片移除并把对方加入好友列表；拒绝后卡片变「已拒绝」；处理中按钮 loading 防重复。

---

### 4.6.5 好友列表

**前端使用场景**：通讯录 / 好友页首屏。

`GET /social/friends`

**是否需要登录**：是　**允许角色**：全部

**成功响应 data**

```json
[
  {
    "userId": 1002,
    "nickname": "小杨",
    "avatarUrl": "https://cdn.example.com/avatar/1002.png",
    "remark": "同班",
    "sessionId": 5001,
    "lastMsgPreview": "明天一起去？",
    "lastMsgAt": "2026-10-26T19:40:00+08:00"
  }
]
```

> `remark ← friendship.remark`；`sessionId` 为与该好友的会话 ID ← `chat_session.id`。

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：按 `lastMsgAt` 倒序；无会话时 `sessionId=null`，点击先进聊天页再由 4.6.7 创建会话；空态给「去加好友」。

---

### 4.6.6 删除好友 / 拉黑 / 取消拉黑

**前端使用场景**：聊天设置页或好友资料页的「删除好友」「加入黑名单」。

`POST /social/friends/{friendUserId}/delete`（删除好友）　`POST /social/blacklist`（拉黑）　`DELETE /social/blacklist/{userId}`（取消拉黑）

**是否需要登录**：是　**允许角色**：全部

**删除好友**：无 body，成功 `data` 为 `null`。

**拉黑 Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| userId | number | 是 | `1002` | 要拉黑的用户 |

**拉黑成功响应 data**

```json
{ "userId": 1002, "blocked": true }
```

**失败示例**

```json
{ "code": 40400, "message": "对方不是你的好友", "data": null }
```

```json
{ "code": 40900, "message": "已经在黑名单中", "data": null }
```

**前端注意**：删除好友**双向移除**，成功后从好友列表移除并关闭会话；拉黑后该好友从列表消失、进「黑名单」页可取消；被拉黑方发消息会失败（见 4.6.8）。

---

### 4.6.7 创建 / 获取会话

**前端使用场景**：点好友「发消息」；或在美食详情页点「问问商家/约饭」发起私聊。

`POST /social/sessions`

**是否需要登录**：是　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| peerUserId | number | 是（私聊） | `1002` | 对方用户 ID |
| sessionType | number | 是 | `1` | `1` 私聊 / `2` 群聊 |
| groupId | number | 群聊必填 | `null` | 群 ID ← `chat_group.id` |

**成功响应 data**

```json
{
  "sessionId": 5001,
  "sessionType": 1,
  "peerUserId": 1002,
  "peerNickname": "小杨",
  "peerAvatarUrl": "https://cdn.example.com/avatar/1002.png"
}
```

**失败示例**

```json
{ "code": 40301, "message": "你已被对方拉黑，无法发起会话", "data": null }
```

```json
{ "code": 40900, "message": "请先添加对方为好友", "data": null }
```

**前端注意**：同一对好友重复调用返回**同一个** `sessionId`，不会新建；拿到后用 4.6.8 拉历史。

---

### 4.6.8 发送消息 / 拉取历史

**前端使用场景**：聊天窗口输入框发送；进入聊天页加载历史、上滑加载更早消息。

`GET /social/sessions/{sessionId}/messages`　`POST /social/sessions/{sessionId}/messages`

**是否需要登录**：是　**允许角色**：全部

**GET Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| beforeMsgId | number | 否 | `90001` | 上滑加载更早消息时传当前最早一条的 `msgId` |
| size | number | 否 | `20` | 默认 20，最大 50 |

**GET 成功响应 data**

```json
{
  "sessionId": 5001,
  "hasMore": true,
  "list": [
    {
      "msgId": 90002,
      "sessionId": 5001,
      "fromUserId": 1002,
      "fromNickname": "小杨",
      "msgType": "TEXT",
      "content": "明天一起去？",
      "createdAt": "2026-10-26T19:40:00+08:00"
    },
    {
      "msgId": 90001,
      "sessionId": 5001,
      "fromUserId": 1001,
      "fromNickname": "我",
      "msgType": "IMAGE",
      "content": "https://cdn.example.com/chat/90001.jpg",
      "createdAt": "2026-10-26T19:38:00+08:00"
    }
  ]
}
```

> `msgId ← chat_message_index.mongo_msg_id`（前端当字符串用），`msgType ← chat_message_index.msg_type`。列表按时间**倒序**返回，前端渲染时反转。

**POST Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| msgType | string | 是 | `TEXT` | `TEXT` 文字 / `IMAGE` 图片 / `EMOJI` 表情 |
| content | string | 是 | `明天一起去？` | `TEXT` ≤ 1000 字；`IMAGE` 传 2.3 上传后的 URL；`EMOJI` 传表情码 |
| clientMsgId | string | 否 | `c-1729939200-1` | 前端生成，用于去重与回显 |

**POST 成功响应 data**

```json
{
  "msgId": "665f2a9c1b2e4d0012ab34cd",
  "clientMsgId": "c-1729939200-1",
  "sessionId": 5001,
  "msgType": "TEXT",
  "content": "明天一起去？",
  "createdAt": "2026-10-26T19:40:00+08:00"
}
```

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

```json
{ "code": 40301, "message": "消息发送失败，对方已将你拉黑", "data": null }
```

```json
{ "code": 40001, "message": "消息内容不能为空", "data": null }
```

**前端注意**

- **每次发送前都可能被拉黑**：收到 `40301` 时该条消息标红显示「发送失败（对方拒收）」，并给重试按钮；不要无限重试。
- 发送采用**乐观渲染**：先用 `clientMsgId` 把气泡插入列表并显示「发送中」，成功后用返回 `msgId` 替换，失败标红。
- 拉历史用 `beforeMsgId` 游标翻页，`hasMore=false` 时停止上滑加载。
- 会话内消息建议 **3–5 秒**轮询增量（或用 WebSocket 时以本文档字段为准）；离开页面即停止轮询。
- 图片消息发送前先走 2.3 上传，拿到 URL 再发。

---

### 4.6.9 黑名单列表

**前端使用场景**：设置页「黑名单」。

`GET /social/blacklist`

**是否需要登录**：是　**允许角色**：全部

**成功响应 data**

```json
[
  {
    "userId": 1003,
    "nickname": "某某",
    "avatarUrl": "https://cdn.example.com/avatar/1003.png",
    "blockedAt": "2026-10-20T10:00:00+08:00"
  }
]
```

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：每项给「移出黑名单」按钮调 `DELETE /social/blacklist/{userId}`；移出后该用户可重新发起申请。

---

## 4.7 拼单约饭

对应页面：拼单列表、发起拼单、拼单详情、我的拼单。流程：填活动信息 → 写入并置「报名中」→ 进入「下一步」等待，四个出口：报名（校验未满且未截止）、管理名单、取消、到点转进行中并结束。

### 4.7.1 拼单列表

**前端使用场景**：拼单页首屏、按状态筛选、下拉加载。

`GET /social/meal-activities`

**是否需要登录**：否（登录后可看「我是否已报名」）　**允许角色**：全部

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| status | number | 否 | `0` | `0` 报名中 / `1` 已满 / `2` 进行中 / `3` 已结束 / `4` 已取消 |
| foodType | string | 否 | `川菜` | ← `meal_activity.food_type` |
| mine | boolean | 否 | `false` | `true` 只看我发起的 |
| joined | boolean | 否 | `false` | `true` 只看我报名的 |
| page / size | number | 否 | `1` / `10` | — |

**成功响应 data**

```json
{
  "list": [
    {
      "activityId": 601,
      "title": "周六一食堂川菜拼单",
      "creatorId": 1001,
      "creatorNickname": "小杨",
      "meetTime": "2026-10-31T12:00:00+08:00",
      "meetPlace": "一食堂二楼",
      "foodType": "川菜",
      "maxMembers": 6,
      "currentMembers": 4,
      "signupDeadline": "2026-10-30T20:00:00+08:00",
      "status": 0,
      "joined": false,
      "isCreator": false
    }
  ],
  "total": 12,
  "page": 1,
  "size": 10
}
```

> `activityId ← meal_activity.id`，`currentMembers` 含发起人；`joined`/`isCreator` 未登录时均为 `false`。

**失败示例**

```json
{ "code": 40001, "message": "status 取值不合法", "data": null }
```

**前端注意**：卡片显示「4/6 人」进度；`status=1 已满`、`3 已结束`、`4 已取消` 的卡片置灰且「报名」按钮禁用；`signupDeadline` 已过时前端直接按「已截止」展示，不必等接口返回。

---

### 4.7.2 发起拼单

**前端使用场景**：拼单页「发起拼单」→ 填表 → 点「发布」。

`POST /social/meal-activities`

**是否需要登录**：是　**允许角色**：全部

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| title | string | 是 | `周六一食堂川菜拼单` | 1–128 字 |
| meetTime | string | 是 | `2026-10-31T12:00:00+08:00` | 必须晚于当前时间 |
| meetPlace | string | 是 | `一食堂二楼` | 1–255 字 |
| foodType | string | 否 | `川菜` | ≤ 64 字 |
| relatedFoodId | number | 否 | `501` | 关联美食 ← `food_item.id` |
| relatedMerchantId | number | 否 | `21` | 关联店铺 ← `merchant.id` |
| maxMembers | number | 是 | `6` | 2–50，含发起人 |
| description | string | 否 | `AA 制，口味可商量` | ≤ 1000 字 |
| signupDeadline | string | 否 | `2026-10-30T20:00:00+08:00` | 必须早于 `meetTime` |

**成功响应 data**

```json
{
  "activityId": 601,
  "status": 0,
  "currentMembers": 1,
  "createdAt": "2026-10-26T20:10:00+08:00"
}
```

**失败示例**

```json
{ "code": 40001, "message": "活动时间必须晚于当前时间", "data": null }
```

```json
{ "code": 40001, "message": "报名截止时间必须早于活动时间", "data": null }
```

**前端注意**：发布成功后跳详情页，发起人自动占 1 人（`currentMembers=1`），「报名」按钮对发起人显示为「管理名单」。

---

### 4.7.3 拼单详情

**前端使用场景**：列表点击进入详情页。

`GET /social/meal-activities/{activityId}`

**是否需要登录**：否　**允许角色**：全部

**成功响应 data**

```json
{
  "activityId": 601,
  "title": "周六一食堂川菜拼单",
  "creatorId": 1001,
  "creatorNickname": "小杨",
  "meetTime": "2026-10-31T12:00:00+08:00",
  "meetPlace": "一食堂二楼",
  "foodType": "川菜",
  "relatedFoodId": 501,
  "relatedFoodName": "麻婆豆腐",
  "relatedMerchantId": 21,
  "maxMembers": 6,
  "currentMembers": 4,
  "description": "AA 制，口味可商量",
  "signupDeadline": "2026-10-30T20:00:00+08:00",
  "status": 0,
  "joined": true,
  "isCreator": false,
  "members": [
    { "userId": 1001, "nickname": "小杨", "avatarUrl": "https://cdn.example.com/a/1001.png", "role": 1, "status": 1 },
    { "userId": 1002, "nickname": "小李", "avatarUrl": "https://cdn.example.com/a/1002.png", "role": 0, "status": 1 }
  ]
}
```

> `members` 只返回 `status=1 已报名` 的成员；`role` 1 发起人 / 0 成员。

**失败示例**

```json
{ "code": 40400, "message": "活动不存在或已取消", "data": null }
```

**前端注意**：`status=0` 且人数已满时，后端可能尚未回写 `1`，前端以 `currentMembers >= maxMembers` 判断并禁用「报名」。

---

### 4.7.4 报名参加

**前端使用场景**：详情页点「我要报名」。

`POST /social/meal-activities/{activityId}/join`

**是否需要登录**：是　**允许角色**：USER

**成功响应 data**

```json
{ "activityId": 601, "joined": true, "currentMembers": 5, "status": 0 }
```

**失败示例**

```json
{ "code": 40900, "message": "活动已满员", "data": null }
```

```json
{ "code": 40900, "message": "报名已截止", "data": null }
```

```json
{ "code": 40900, "message": "你已报名该活动", "data": null }
```

```json
{ "code": 40900, "message": "不能报名自己发起的活动", "data": null }
```

**前端注意**

- 报名前置校验：`status!=0`、`currentMembers>=maxMembers`、`signupDeadline` 已过 → 按钮直接禁用并给对应文案，减少无效请求。
- 已满时**只禁止报名**，发起者的「管理名单」「取消活动」入口仍要保持可用。
- 报名成功后本地把 `currentMembers` 加 1、按钮变「已报名」；若返回 `status=1`，提示「已满员，报名成功但活动已停止招募」。

---

### 4.7.5 退出拼单

**前端使用场景**：普通成员在详情页点「退出」。

`POST /social/meal-activities/{activityId}/quit`

**是否需要登录**：是　**允许角色**：USER

**成功响应 data**

```json
{ "activityId": 601, "joined": false, "currentMembers": 4 }
```

**失败示例**

```json
{ "code": 40900, "message": "活动已开始，无法退出", "data": null }
```

```json
{ "code": 40900, "message": "发起人不能退出，请取消活动", "data": null }
```

**前端注意**：退出需二次确认；`status>=2`（进行中/已结束）时按钮禁用。

---

### 4.7.6 管理名单（移出成员）

**前端使用场景**：发起人在详情页「管理名单」，成员行右侧「移出」。

`GET /social/meal-activities/{activityId}/members`　`DELETE /social/meal-activities/{activityId}/members/{userId}`

**是否需要登录**：是　**允许角色**：USER（仅发起人）

**GET 成功响应 data**

```json
[
  { "userId": 1001, "nickname": "小杨", "role": 1, "status": 1, "joinedAt": "2026-10-26T20:10:00+08:00" },
  { "userId": 1003, "nickname": "小王", "role": 0, "status": 3, "joinedAt": "2026-10-26T20:20:00+08:00" }
]
```

**移出成功响应 data**

```json
{ "activityId": 601, "userId": 1003, "currentMembers": 4 }
```

**失败示例**

```json
{ "code": 40300, "message": "只有发起人可以管理名单", "data": null }
```

```json
{ "code": 40900, "message": "活动已结束，无法调整名单", "data": null }
```

**前端注意**：移出后本地更新 `currentMembers`；被移出成员 `status=3` 灰显；发起人自己不可被移出。

---

### 4.7.7 取消活动

**前端使用场景**：发起人在详情页点「取消活动」。

`POST /social/meal-activities/{activityId}/cancel`

**是否需要登录**：是　**允许角色**：USER（仅发起人）

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| reason | string | 否 | `临时有事` | ≤ 255 字 |

**成功响应 data**

```json
{ "activityId": 601, "status": 4 }
```

**失败示例**

```json
{ "code": 40300, "message": "只有发起人可以取消活动", "data": null }
```

```json
{ "code": 40900, "message": "活动已结束，无需取消", "data": null }
```

**前端注意**：二次确认弹窗写清「取消后已报名成员会收到通知」；成功后整页置为「已取消」状态，所有操作按钮禁用。

---

### 4.7.8 活动反馈

**前端使用场景**：活动结束后（`status=3`），参与者在详情页「评价这次约饭」。

`POST /social/meal-activities/{activityId}/feedback`　`GET /social/meal-activities/{activityId}/feedback`

**是否需要登录**：是　**允许角色**：USER（参与者）

**POST Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| toUserId | number | 否 | `1002` | 不传表示对活动的整体反馈 |
| score | number | 是 | `5` | 1–5 |
| content | string | 否 | `氛围很好` | ≤ 500 字 |

**POST 成功响应 data**

```json
{ "feedbackId": 1201, "activityId": 601 }
```

**GET 成功响应 data**

```json
[
  { "feedbackId": 1201, "fromUserId": 1001, "fromNickname": "小杨", "toUserId": null, "score": 5, "content": "氛围很好", "createdAt": "2026-10-31T14:00:00+08:00" }
]
```

**失败示例**

```json
{ "code": 40900, "message": "活动尚未结束，暂不能评价", "data": null }
```

```json
{ "code": 40300, "message": "只有参与成员可以评价", "data": null }
```

**前端注意**：`status!=3` 时隐藏评价入口；已评价过的用户按钮改「已评价」禁用。
---

## 4.8 下单与订单

对应页面：购物车、确认订单、收银台、我的订单、订单详情、商家接单台。
状态机：`PENDING_PAY`（待支付）→ `PAID`（已支付待接单）→ `ACCEPTED`（已接单）→ `COMPLETED`（已完成）；支付超时、商家拒单、用户取消 → `CANCELLED`。

> **重要**：下单、支付、订单查询、商家接单/取消/完成**全部走 `/order/**`**，不要调 `/merchant/**`。`/merchant/**` 只负责店铺、菜品、营销、评价回复。

### 4.8.1 购物车列表

**前端使用场景**：进购物车页；从详情页「加入购物车」后返回。

`GET /order/cart`

**是否需要登录**：是　**允许角色**：USER

**成功响应 data**

```json
{
  "items": [
    {
      "cartItemId": 801,
      "foodId": 501,
      "foodName": "麻婆豆腐",
      "merchantId": 21,
      "shopName": "川味小馆",
      "coverUrl": "https://cdn.example.com/food/501.jpg",
      "unitPrice": "12.00",
      "quantity": 2,
      "stock": 38,
      "available": true,
      "unavailableReason": null
    }
  ],
  "totalQuantity": 2,
  "totalAmount": "24.00"
}
```

> `available=false` 时 `unavailableReason` 为 `OFF_SHELF` 已下架 / `OUT_OF_STOCK` 库存不足 / `SHOP_CLOSED` 店铺休息。

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：`available=false` 的行灰显且不参与合计；`totalAmount` 由服务端算，前端只用它展示，不要本地累加。

---

### 4.8.2 加入购物车 / 修改数量 / 移除

**前端使用场景**：详情页「加入购物车」、购物车 +/− 与删除。

`POST /order/cart`（加入）　`PUT /order/cart/{cartItemId}`（改数量）　`DELETE /order/cart/{cartItemId}`（移除）

**是否需要登录**：是　**允许角色**：USER

**POST Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| foodId | number | 是 | `501` | ← `food_item.id`，须上架 |
| quantity | number | 是 | `2` | 1–99，且 ≤ 当前库存 |

**PUT Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| quantity | number | 是 | `3` | 1–99，且 ≤ 当前库存 |

**成功响应 data**

```json
{ "cartItemId": 801, "foodId": 501, "quantity": 2, "totalQuantity": 2, "totalAmount": "24.00" }
```

**失败示例**

```json
{ "code": 40900, "message": "库存不足，当前仅剩 1 份", "data": null }
```

```json
{ "code": 40900, "message": "该美食已下架", "data": null }
```

```json
{ "code": 40001, "message": "购买数量不能超过 99", "data": null }
```

**前端注意**：数量 + 到库存上限时按钮禁用并提示；下架/售罄时「加入购物车」置灰；同一菜品重复加入应累加数量而不是新增一行。

---

### 4.8.3 结算预览（确认订单页）

**前端使用场景**：购物车点「去结算」进入确认订单页，展示可用优惠、配送方式、应付金额。

`POST /order/checkout/preview`

**是否需要登录**：是　**允许角色**：USER

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| cartItemIds | number[] | 是 | `[801,802]` | 至少 1 项，须属于当前用户 |
| deliveryType | number | 是 | `2` | `1` 到店自提 / `2` 配送到寝 |
| deliveryAddress | string | 配送必填 | `3 号宿舍楼 502` | ≤ 255 字，`deliveryType=2` 时必填 |

**成功响应 data**

```json
{
  "items": [
    { "foodId": 501, "foodName": "麻婆豆腐", "unitPrice": "12.00", "quantity": 2, "amount": "24.00" }
  ],
  "goodsAmount": "24.00",
  "discountAmount": "4.00",
  "payAmount": "20.00",
  "deliveryType": 2,
  "deliveryFee": "0.00",
  "availableCampaigns": [
    { "campaignId": 301, "title": "满 20 减 4", "campaignType": "FULL_REDUCE", "discountAmount": "4.00" }
  ],
  "campaignId": 301,
  "estimateMinutes": 25
}
```

**失败示例**

```json
{ "code": 40900, "message": "「麻婆豆腐」已下架，请移除后重试", "data": null }
```

```json
{ "code": 40900, "message": "「麻婆豆腐」库存不足，当前仅剩 1 份", "data": null }
```

```json
{ "code": 40001, "message": "配送到寝必须填写收货地址", "data": null }
```

**前端注意**：预览失败要**指明是哪道菜**（`message` 已含菜名），前端高亮对应行并给「移除」按钮；切换配送方式/地址/优惠券都要重新预览，不要本地算钱；`payAmount` 只用服务端值。

---

### 4.8.4 提交订单

**前端使用场景**：确认订单页点「提交订单」。

`POST /order/orders`

**是否需要登录**：是　**允许角色**：USER

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| cartItemIds | number[] | 是 | `[801]` | 与预览一致 |
| deliveryType | number | 是 | `2` | `1` 自提 / `2` 配送 |
| deliveryAddress | string | 配送必填 | `3 号宿舍楼 502` | ≤ 255 字 |
| campaignId | number | 否 | `301` | 使用的营销活动 ← `marketing_campaign.id` |
| remark | string | 否 | `少放辣` | ≤ 255 字 |
| clientOrderToken | string | 是 | `t-1729939200-abc` | 前端生成的幂等令牌，防止重复下单 |

**成功响应 data**

```json
{
  "orderId": 7001,
  "orderNo": "CF20261026201000001",
  "status": "PENDING_PAY",
  "payAmount": "20.00",
  "expireAt": "2026-10-26T20:30:00+08:00"
}
```

> `orderId ← food_order.id`、`orderNo ← food_order.order_no`、`status ← food_order.status`、`payAmount ← food_order.pay_amount`。

**失败示例**

```json
{ "code": 40900, "message": "库存不足，下单失败", "data": null }
```

```json
{ "code": 40900, "message": "该美食已下架，无法下单", "data": null }
```

```json
{ "code": 40900, "message": "请勿重复提交订单", "data": null }
```

**前端注意**

- 提交按钮点击后**立即禁用**，并带 `clientOrderToken`；重复点击由服务端返回 `40900 请勿重复提交`，前端不要重试而是跳订单详情。
- 成功后直接跳收银台（`expireAt` 前完成支付），订单列表用 `PENDING_PAY` 展示倒计时。
- `库存不足` 提示要带菜名，并给「返回购物车调整」。

---

### 4.8.5 发起支付

**前端使用场景**：收银台选择支付方式点「立即支付」。

`POST /order/orders/{orderId}/pay`

**是否需要登录**：是　**允许角色**：USER

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| payChannel | string | 是 | `MOCK` | `MOCK` 模拟 / `WECHAT` 微信 / `ALIPAY` 支付宝 |

**成功响应 data**

```json
{
  "orderId": 7001,
  "paymentId": 1301,
  "payChannel": "MOCK",
  "amount": "20.00",
  "status": 1,
  "paidAt": "2026-10-26T20:12:30+08:00",
  "orderStatus": "PAID"
}
```

**失败示例**

```json
{ "code": 40900, "message": "订单已支付，请勿重复支付", "data": null }
```

```json
{ "code": 40900, "message": "订单已超时取消，请重新下单", "data": null }
```

```json
{ "code": 40300, "message": "无权操作该订单", "data": null }
```

**前端注意**：支付成功后 `orderStatus` 变 `PAID`，立即跳订单详情并轮询状态；支付失败不要自行改单状态，以 4.8.7 查询为准。

---

### 4.8.6 我的订单列表

**前端使用场景**：个人中心「我的订单」、订单页 Tab 切换。

`GET /order/orders`

**是否需要登录**：是　**允许角色**：USER

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| status | string | 否 | `PAID` | 见 2.6 订单状态，不传为全部 |
| page / size | number | 否 | `1` / `10` | — |

**成功响应 data**

```json
{
  "list": [
    {
      "orderId": 7001,
      "orderNo": "CF20261026201000001",
      "merchantId": 21,
      "shopName": "川味小馆",
      "coverUrl": "https://cdn.example.com/food/501.jpg",
      "itemSummary": "麻婆豆腐 等 2 件",
      "itemCount": 2,
      "payAmount": "20.00",
      "deliveryType": 2,
      "status": "PAID",
      "statusText": "已支付待接单",
      "createdAt": "2026-10-26T20:10:00+08:00",
      "expireAt": "2026-10-26T20:30:00+08:00"
    }
  ],
  "total": 3,
  "page": 1,
  "size": 10
}
```

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：`PENDING_PAY` 的卡片显示剩余支付时间倒计时，到 0 后自动刷新该条（服务端会置 `CANCELLED`）；`statusText` 为服务端给的中文，可直接展示。

---

### 4.8.7 订单详情

**前端使用场景**：订单列表点击进入详情页；支付后轮询。

`GET /order/orders/{orderId}`

**是否需要登录**：是　**允许角色**：USER（本人）/ MERCHANT（本店订单）/ ADMIN

**成功响应 data**

```json
{
  "orderId": 7001,
  "orderNo": "CF20261026201000001",
  "merchantId": 21,
  "shopName": "川味小馆",
  "items": [
    { "foodId": 501, "foodName": "麻婆豆腐", "unitPrice": "12.00", "quantity": 2, "amount": "24.00" }
  ],
  "goodsAmount": "24.00",
  "discountAmount": "4.00",
  "payAmount": "20.00",
  "campaignTitle": "满 20 减 4",
  "deliveryType": 2,
  "deliveryAddress": "3 号宿舍楼 502",
  "remark": "少放辣",
  "status": "ACCEPTED",
  "statusText": "已接单",
  "timeline": [
    { "status": "PENDING_PAY", "at": "2026-10-26T20:10:00+08:00" },
    { "status": "PAID", "at": "2026-10-26T20:12:30+08:00" },
    { "status": "ACCEPTED", "at": "2026-10-26T20:14:00+08:00" }
  ],
  "cancelReason": null,
  "createdAt": "2026-10-26T20:10:00+08:00"
}
```

**失败示例**

```json
{ "code": 40300, "message": "无权查看该订单", "data": null }
```

```json
{ "code": 40400, "message": "订单不存在", "data": null }
```

**前端注意**：`status IN (PENDING_PAY, PAID, ACCEPTED)` 时详情页每 **10 秒**轮询一次，离开页面或到 `COMPLETED/CANCELLED` 即停止；`timeline` 用于进度条，缺失节点不显示。

---

### 4.8.8 取消订单

**前端使用场景**：用户在自己订单详情点「取消订单」；商家在接单台点「拒单」。

`POST /order/orders/{orderId}/cancel`

**是否需要登录**：是　**允许角色**：USER（本人未接单前）/ MERCHANT（本店未接单前）

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| reason | string | 是 | `临时有事` | 1–255 字 |

**成功响应 data**

```json
{ "orderId": 7001, "status": "CANCELLED", "cancelledAt": "2026-10-26T20:16:00+08:00" }
```

**失败示例**

```json
{ "code": 40900, "message": "商家已接单，无法取消", "data": null }
```

```json
{ "code": 40900, "message": "订单已完成，无法取消", "data": null }
```

**前端注意**：仅 `PENDING_PAY`、`PAID` 可取消（`PAID` 取消后进入退款）；`ACCEPTED` 之后按钮隐藏，改为「联系商家」；取消后列表与详情要刷新。

---

### 4.8.9 商家接单 / 完成订单

**前端使用场景**：商家后台「订单管理」列表点「接单」「完成出餐」。

`POST /order/orders/{orderId}/accept`　`POST /order/orders/{orderId}/complete`

**是否需要登录**：是　**允许角色**：MERCHANT（本店订单）/ ADMIN

**Body**：无

**accept 成功响应 data**

```json
{ "orderId": 7001, "status": "ACCEPTED", "acceptedAt": "2026-10-26T20:14:00+08:00" }
```

**complete 成功响应 data**

```json
{ "orderId": 7001, "status": "COMPLETED", "completedAt": "2026-10-26T20:35:00+08:00" }
```

**失败示例**

```json
{ "code": 40300, "message": "该订单不属于你的店铺", "data": null }
```

```json
{ "code": 40900, "message": "订单未支付，不能接单", "data": null }
```

```json
{ "code": 40900, "message": "订单状态已变更，请刷新", "data": null }
```

**前端注意**：商家订单台建议 **30 秒**轮询新订单（`status=PAID`），有新增时播放提示音并可加角标；操作成功后局部更新该行，不要整页刷新；`订单状态已变更` 时立即重新拉列表。

---

### 4.8.10 商家订单列表

**前端使用场景**：商家后台订单页。

`GET /order/merchant/orders`

**是否需要登录**：是　**允许角色**：MERCHANT

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| status | string | 否 | `PAID` | 同上 |
| page / size | number | 否 | `1` / `10` | — |

**成功响应 data**：结构同 4.8.6（多返回 `deliveryAddress`、`remark`）。

**失败示例**

```json
{ "code": 40300, "message": "仅商家可访问", "data": null }
```

**前端注意**：`PAID` 为「待接单」高亮，配「接单」按钮；`PAID` 超过 5 分钟未接单可标红提醒。

---

## 4.9 商家入驻与商品/经营

对应页面：商家入驻页、「我的店铺」、菜品管理、营销活动、评价回复、经营统计。
**下单主流程不在这里**（见 4.8）。

### 4.9.1 提交入驻申请

**前端使用场景**：商家端「入驻申请」页填表提交。

`POST /merchant/insert`

**是否需要登录**：是　**允许角色**：USER（提交后等审核通过升为 MERCHANT）

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| shopName | string | 是 | `川味小馆` | 1–128 字，不可重名 |
| campusArea | string | 否 | `一食堂` | ≤ 64 字 |
| address | string | 是 | `一食堂二楼 12 号窗口` | 1–255 字 |
| contactPhone | string | 是 | `13800138001` | 手机号格式 |
| logoUrl | string | 否 | `https://...` | 先走 2.3 上传 |
| description | string | 否 | `主打川菜` | ≤ 1000 字 |
| licenseImgUrl | string | 是 | `https://...` | 营业执照，先走 2.3 上传 |

**成功响应 data**

```json
{ "merchantId": 21, "auditStatus": 0, "status": 0 }
```

**失败示例**

```json
{ "code": 40001, "message": "请上传营业执照", "data": null }
```

```json
{ "code": 40900, "message": "你已提交过入驻申请，请等待审核", "data": null }
```

**前端注意**：提交后跳「入驻进度」页显示 `0 待审核`；审核结果通过站内信通知；被驳回（`auditStatus=2`）可修改后重新提交。

---

### 4.9.2 入驻进度 / 我的店铺

**前端使用场景**：商家端首页、入驻进度页。

`GET /merchant/mine`

**是否需要登录**：是　**允许角色**：MERCHANT（含待审的 USER）

**成功响应 data**

```json
[
  {
    "merchantId": 21,
    "shopName": "川味小馆",
    "campusArea": "一食堂",
    "address": "一食堂二楼 12 号窗口",
    "contactPhone": "13800138001",
    "logoUrl": "https://cdn.example.com/shop/21.png",
    "description": "主打川菜",
    "auditStatus": 1,
    "auditRemark": null,
    "status": 1,
    "settledAt": "2026-10-20T09:00:00+08:00"
  }
]
```

> `auditStatus ← merchant.audit_status`（0 待审 / 1 通过 / 2 驳回 / 3 取消资格），`status ← merchant.status`（0 未开业 / 1 营业 / 2 停业）。

**失败示例**

```json
{ "code": 40100, "message": "请先登录", "data": null }
```

**前端注意**：`auditStatus=2` 时展示 `auditRemark` 并给「修改重提」；`auditStatus!=1` 时菜品/订单/营销菜单全部置灰，点了提示「审核通过后可用」。

---

### 4.9.3 修改店铺信息

**前端使用场景**：店铺设置页点「保存」。

`PUT /merchant/update`

**是否需要登录**：是　**允许角色**：MERCHANT（仅本人店铺）

**Body**：`merchantId` 必填，其余只传要改的字段（同 4.9.1 字段）。

**成功响应 data**

```json
{ "merchantId": 21 }
```

**失败示例**

```json
{ "code": 40300, "message": "只能修改自己的店铺", "data": null }
```

```json
{ "code": 40900, "message": "店铺已取消资格，无法修改", "data": null }
```

**前端注意**：修改店名/证照后 `auditStatus` 会回到 `0 待审`，要提示「修改后需重新审核」；营业状态开关（营业/停业）单独走 4.9.4。

---

### 4.9.4 营业状态开关

**前端使用场景**：商家后台右上角「营业中/休息中」开关。

`PUT /merchant/status`

**是否需要登录**：是　**允许角色**：MERCHANT

**Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| status | number | 是 | `1` | `1` 营业 / `2` 停业 |

**成功响应 data**

```json
{ "merchantId": 21, "status": 2 }
```

**失败示例**

```json
{ "code": 40900, "message": "店铺未通过审核，不能开业", "data": null }
```

**前端注意**：停业后用户端该店菜品显示「店铺休息中」且不可下单，开关切换要乐观更新 + 失败回滚。

---

### 4.9.5 菜品列表（商家）

**前端使用场景**：商家后台「菜品管理」列表、按状态筛选。

`GET /merchant/foods/mine`

**是否需要登录**：是　**允许角色**：MERCHANT

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| status | number | 否 | `1` | `0` 草稿 / `1` 上架 / `2` 下架 |
| keyword | string | 否 | `麻婆` | 名称模糊 |
| page / size | number | 否 | `1` / `10` | — |

**成功响应 data**

```json
{
  "list": [
    {
      "foodId": 501,
      "name": "麻婆豆腐",
      "categoryId": 11,
      "foodType": "川菜",
      "price": "12.00",
      "stock": 38,
      "coverUrl": "https://cdn.example.com/food/501.jpg",
      "avgScore": 4.70,
      "reviewCount": 26,
      "salesCount": 412,
      "recommendTag": "招牌",
      "status": 1,
      "tasteTags": "辣,麻",
      "ingredients": "豆腐,牛肉末"
    }
  ],
  "total": 12,
  "page": 1,
  "size": 10
}
```

**失败示例**

```json
{ "code": 40300, "message": "店铺未通过审核", "data": null }
```

**前端注意**：`status` 用徽标区分（草稿/上架/下架）；列表内直接给「上架/下架」「编辑」「删除」操作。

---

### 4.9.6 新增 / 编辑 / 上下架 / 删除菜品

**前端使用场景**：菜品管理点「新增菜品」「编辑」「上架」「下架」「删除」。

`POST /merchant/foods/save`　`PUT /merchant/foods/update/{foodId}`　`PUT /merchant/foods/onshelf/{foodId}`　`PUT /merchant/foods/offshelf/{foodId}`　`PUT /merchant/foods/delete/{foodId}`

**是否需要登录**：是　**允许角色**：MERCHANT（仅本人店铺菜品）

**save / update Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| name | string | 是 | `麻婆豆腐` | 1–128 字 |
| categoryId | number | 否 | `11` | ← `food_category.id` |
| foodType | string | 否 | `川菜` | ≤ 64 字 |
| price | string | 是 | `12.00` | > 0，两位小数 |
| stock | number | 是 | `38` | ≥ 0 整数 |
| description | string | 否 | `地道川味` | ≤ 2000 字 |
| coverUrl | string | 否 | `https://...` | 先走 2.3 上传 |
| tasteTags | string[] | 否 | `["辣","麻"]` | ≤ 10 个 |
| ingredients | string[] | 否 | `["豆腐","牛肉末"]` | ≤ 20 个 |
| recommendTag | string | 否 | `招牌` | ≤ 128 字 |

**save 成功响应 data**

```json
{ "foodId": 502, "status": 1 }
```

**上下架 / 删除成功响应 data**

```json
{ "foodId": 501, "status": 2 }
```

**失败示例**

```json
{ "code": 40001, "message": "价格必须大于 0", "data": null }
```

```json
{ "code": 40300, "message": "只能操作自己店铺的菜品", "data": null }
```

```json
{ "code": 40900, "message": "该菜品有进行中的订单，暂不能删除", "data": null }
```

**前端注意**：新增后默认「上架」；删除为逻辑删除，需二次确认并提示「删除后用户端不可见」；改价格只影响新订单，已在购物车的按结算时最新价提示差异。

---

### 4.9.7 营销活动

**前端使用场景**：商家后台「营销活动」列表与新建。

`GET /merchant/campaigns`　`POST /merchant/campaigns`　`PUT /merchant/campaigns/{campaignId}/offline`

**是否需要登录**：是　**允许角色**：MERCHANT

**POST Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| title | string | 是 | `满 20 减 4` | 1–128 字 |
| campaignType | string | 是 | `FULL_REDUCE` | `DISCOUNT` / `FULL_REDUCE` / `GROUP_BUY` |
| rule | object | 是 | `{"threshold":"20.00","reduce":"4.00"}` | 折扣填 `{"discount":0.85}` |
| startAt | string | 是 | `2026-10-26T00:00:00+08:00` | 早于 `endAt` |
| endAt | string | 是 | `2026-10-31T23:59:59+08:00` | 晚于 `startAt` |
| foodIds | number[] | 否 | `[501,502]` | 参与菜品，不传为全店 |

**GET 成功响应 data**

```json
{
  "list": [
    {
      "campaignId": 301,
      "title": "满 20 减 4",
      "campaignType": "FULL_REDUCE",
      "rule": { "threshold": "20.00", "reduce": "4.00" },
      "startAt": "2026-10-26T00:00:00+08:00",
      "endAt": "2026-10-31T23:59:59+08:00",
      "status": 1,
      "participantCount": 18,
      "orderCount": 12,
      "salesAmount": "286.00"
    }
  ],
  "total": 3,
  "page": 1,
  "size": 10
}
```

**失败示例**

```json
{ "code": 40001, "message": "结束时间必须晚于开始时间", "data": null }
```

```json
{ "code": 40900, "message": "该时间段已有进行中的活动", "data": null }
```

**前端注意**：`status=1` 才在前端标「进行中」；下线需二次确认，下线后用户端立即不可用；活动统计（参与人数/订单数/销售额）每分钟刷新一次即可。

---

### 4.9.8 评价回复（商家）

**前端使用场景**：商家后台「评价管理」列表，点「回复」。

`GET /merchant/reviews`（本店评价）　`POST /merchant/reviews/{reviewId}/reply`

**是否需要登录**：是　**允许角色**：MERCHANT

**GET Query**：`score`（1–5）、`hasReply`（boolean）、`page`、`size`

**GET 成功响应 data**

```json
{
  "list": [
    {
      "reviewId": 9001,
      "foodId": 501,
      "foodName": "麻婆豆腐",
      "nickname": "小杨",
      "score": 5,
      "content": "分量足",
      "images": ["https://cdn.example.com/review/9001-1.jpg"],
      "createdAt": "2026-10-20T12:30:00+08:00",
      "replied": false
    }
  ],
  "total": 26,
  "page": 1,
  "size": 10
}
```

**POST Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| content | string | 是 | `感谢支持！` | 1–500 字 |

**POST 成功响应 data**

```json
{ "replyId": 3002, "reviewId": 9001, "createdAt": "2026-10-26T20:40:00+08:00" }
```

**失败示例**

```json
{ "code": 40300, "message": "该评价不属于你的店铺", "data": null }
```

```json
{ "code": 40900, "message": "已回复过该评价", "data": null }
```

**前端注意**：`replied=true` 的条目回复按钮改「查看回复」；回复内容对外展示，提示「请文明回复」。

---

### 4.9.9 经营统计

**前端使用场景**：商家后台首页数据卡片与趋势图。

`GET /merchant/stats/daily`

**是否需要登录**：是　**允许角色**：MERCHANT

**Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| from | string | 是 | `2026-10-01` | 起始日期 |
| to | string | 是 | `2026-10-26` | 结束日期，区间 ≤ 90 天 |

**成功响应 data**

```json
{
  "summary": { "orderCount": 128, "salesAmount": "2860.00", "avgScore": 4.70, "reviewCount": 96 },
  "list": [
    { "date": "2026-10-26", "orderCount": 12, "salesAmount": "286.00" }
  ]
}
```

**失败示例**

```json
{ "code": 40001, "message": "查询区间不能超过 90 天", "data": null }
```

**前端注意**：无数据日期服务端补 0，前端可直接画折线；金额用字符串避免精度问题。

---

## 4.10 平台管理

对应页面：管理后台首页、商家审核、内容审核、用户管理、公告管理、推荐位、数据统计。**处罚只有「警告」和「封禁」两档，本期没有「限制」。**

### 4.10.1 商家入驻审核

**前端使用场景**：管理后台「商家审核」列表，点「通过」「驳回」。

`GET /admin/merchants/pending`　`PUT /admin/merchants/{merchantId}/approve`　`PUT /admin/merchants/{merchantId}/reject`

**是否需要登录**：是　**允许角色**：ADMIN

**GET 成功响应 data**

```json
[
  {
    "merchantId": 22,
    "shopName": "粥铺",
    "ownerUserId": 1005,
    "ownerNickname": "老王",
    "campusArea": "二食堂",
    "address": "二食堂一楼 3 号",
    "contactPhone": "13800138002",
    "licenseImgUrl": "https://cdn.example.com/lic/22.jpg",
    "auditStatus": 0,
    "createdAt": "2026-10-25T10:00:00+08:00"
  }
]
```

**approve Body**（可选）

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| auditRemark | string | 否 | `资料齐全` | ≤ 512 字 |

**reject Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| reason | string | 是 | `营业执照不清晰` | 1–200 字 |

**成功响应 data**

```json
{ "merchantId": 22, "auditStatus": 1, "status": 1, "settledAt": "2026-10-26T20:50:00+08:00" }
```

**失败示例**

```json
{ "code": 40300, "message": "无管理员权限", "data": null }
```

```json
{ "code": 40900, "message": "该商家已审核，请勿重复操作", "data": null }
```

**前端注意**：通过后店主角色由 USER 升为 MERCHANT（服务端完成，前端刷新列表即可）；驳回原因必填，并会以站内信通知商家。

---

### 4.10.2 用户管理

**前端使用场景**：管理后台「用户管理」列表、用户详情、处罚操作。

`GET /admin/users`　`GET /admin/users/{userId}`　`POST /admin/users/{userId}/punishments`

**是否需要登录**：是　**允许角色**：ADMIN

**GET Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| keyword | string | 否 | `小杨` 或 `13800138000` | 昵称模糊 / 手机号精确 |
| status | number | 否 | `1` | `0` 禁用 / `1` 正常 |
| role | string | 否 | `USER` | `USER` / `MERCHANT` / `ADMIN` |
| page / size | number | 否 | `1` / `10` | — |

**GET 成功响应 data**

```json
{
  "list": [
    {
      "userId": 1001,
      "nickname": "小杨",
      "phone": "138****8000",
      "role": "USER",
      "status": 1,
      "punishCount": 1,
      "lastPunishment": "WARN",
      "createdAt": "2026-09-01T09:00:00+08:00"
    }
  ],
  "total": 56,
  "page": 1,
  "size": 10
}
```

**处罚 Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| action | string | 是 | `BAN` | `WARN` 警告 / `BAN` 封禁 |
| reason | string | 是 | `发布虚假评价` | 1–512 字 |
| expireAt | string | 否 | `null` | 空为永久，仅 `BAN` 有效 |

**处罚成功响应 data**

```json
{
  "punishmentId": 501,
  "userId": 1001,
  "action": "BAN",
  "userStatus": 0,
  "createdAt": "2026-10-26T20:55:00+08:00"
}
```

**失败示例**

```json
{ "code": 40001, "message": "处罚类型只能是 WARN 或 BAN", "data": null }
```

```json
{ "code": 40900, "message": "该用户已被封禁，请勿重复操作", "data": null }
```

**前端注意**

- **警告（`WARN`）不改登录状态**：`userStatus` 仍为 `1`，只推送站内信；前端不要显示「已封禁」。
- **封禁（`BAN`）置 `status=0`**：`userStatus=0`，该用户无法再登录（登录返回 `40102`）。
- 处罚按钮必须二次确认并展示 `reason` 输入框；管理员自己不可被处罚（前端隐藏入口）。
- 处罚记录可展开查看历史（`GET /admin/users/{userId}/punishments`，返回列表结构同上）。

---

### 4.10.3 内容审核

**前端使用场景**：管理后台「内容审核」列表，审核评价/动态/店铺资料。

`GET /admin/audits`　`PUT /admin/audits/{auditId}`

**是否需要登录**：是　**允许角色**：ADMIN

**GET Query**

| 字段 | 类型 | 必填 | 示例 | 说明 |
| --- | --- | --- | --- | --- |
| contentType | string | 否 | `REVIEW` | `REVIEW` / `REPLY` / `FEED` / `MERCHANT_INFO` / `IMAGE` |
| status | number | 否 | `0` | `0` 待审 / `1` 通过 / `2` 拒绝 |
| page / size | number | 否 | `1` / `10` | — |

**GET 成功响应 data**

```json
{
  "list": [
    {
      "auditId": 2101,
      "contentType": "REVIEW",
      "contentId": 9001,
      "title": "麻婆豆腐 · 5 星评价",
      "snapshot": "分量足，微辣刚好。",
      "submitUserId": 1001,
      "submitNickname": "小杨",
      "status": 0,
      "createdAt": "2026-10-26T12:30:00+08:00"
    }
  ],
  "total": 8,
  "page": 1,
  "size": 10
}
```

**PUT Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| status | number | 是 | `1` | `1` 通过 / `2` 拒绝 |
| auditRemark | string | 拒绝必填 | `含广告内容` | ≤ 512 字 |

**PUT 成功响应 data**

```json
{ "auditId": 2101, "status": 2, "auditedAt": "2026-10-26T21:00:00+08:00" }
```

**失败示例**

```json
{ "code": 40900, "message": "该内容已审核", "data": null }
```

```json
{ "code": 40001, "message": "拒绝时必须填写审核意见", "data": null }
```

**前端注意**：通过后评价才会在用户端展示；拒绝时 `auditRemark` 会推送给作者；审核列表支持快捷键（通过/拒绝）时注意二次确认。

---

### 4.10.4 公告管理

**前端使用场景**：管理后台「公告管理」列表与编辑。

`GET /admin/notices`　`POST /admin/notices`　`PUT /admin/notices/{noticeId}`　`DELETE /admin/notices/{noticeId}`

**是否需要登录**：是　**允许角色**：ADMIN

**POST / PUT Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| title | string | 是 | `国庆营业时间调整` | 1–128 字 |
| content | string | 是 | `10 月 1 日至 3 日……` | 1–5000 字 |
| publishStatus | number | 是 | `1` | `0` 存草稿 / `1` 立即发布 |

**POST 成功响应 data**

```json
{ "noticeId": 4, "publishStatus": 1, "publishedAt": "2026-10-26T21:05:00+08:00" }
```

**失败示例**

```json
{ "code": 40001, "message": "公告标题不能为空", "data": null }
```

**前端注意**：`publishStatus=1` 才会出现在用户端公告；删除为逻辑删除，需二次确认。

---

### 4.10.5 推荐位配置

**前端使用场景**：管理后台「推荐位」页配置首页 Banner 与推荐位内容。

`GET /admin/recommend-slots`　`PUT /admin/recommend-slots/{slotId}`

**是否需要登录**：是　**允许角色**：ADMIN

**PUT Body**

| 字段 | 类型 | 必填 | 示例 | 校验规则 |
| --- | --- | --- | --- | --- |
| title | string | 是 | `首页 Banner` | ≤ 128 字 |
| imageUrl | string | 否 | `https://...` | 先走 2.3 上传 |
| targetType | string | 是 | `FOOD` | `FOOD` 美食 / `MERCHANT` 店铺 / `URL` 外链 |
| targetId | number | 否 | `501` | `targetType!=URL` 时必填 |
| sortNo | number | 是 | `1` | 越小越靠前 |
| enabled | boolean | 是 | `true` | 是否启用 |

**成功响应 data**

```json
{ "slotId": 11, "targetType": "FOOD", "targetId": 501, "sortNo": 1, "enabled": true }
```

**失败示例**

```json
{ "code": 40001, "message": "选择美食或店铺时必须指定目标 ID", "data": null }
```

**前端注意**：排序值变更后用户端首页下一次刷新生效；禁用后立即从首页移除。

---

### 4.10.6 数据统计

**前端使用场景**：管理后台首页概览与报表导出。

`GET /admin/stats/overview`　`GET /admin/stats/daily`

**是否需要登录**：是　**允许角色**：ADMIN

**overview 成功响应 data**

```json
{
  "userCount": 1280,
  "merchantCount": 36,
  "todayOrderCount": 128,
  "todaySalesAmount": "2860.00",
  "pendingAuditCount": 8,
  "pendingMerchantCount": 2
}
```

**daily Query**：`from`、`to`（区间 ≤ 90 天）

**daily 成功响应 data**

```json
{
  "list": [
    { "date": "2026-10-26", "userCount": 12, "orderCount": 128, "salesAmount": "2860.00" }
  ]
}
```

**失败示例**

```json
{ "code": 40001, "message": "查询区间不能超过 90 天", "data": null }
```

**前端注意**：概览页每 60 秒刷新；`pendingAuditCount`、`pendingMerchantCount` 用于侧边栏红点；导出走 `GET /admin/stats/daily/export?from=&to=`，直接返回 Excel 流（`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`），前端用 `blob` 下载。
---

## 5 页面 → 接口对照表

| 页面 / 组件 | 主要接口 | 备注 |
| --- | --- | --- |
| 登录页 | `POST /auth/login`、`POST /auth/code` | 40102 停留本页 |
| 注册页 | `POST /auth/code`、`POST /auth/register` | 注册即登录 |
| 忘记密码页 | `POST /auth/code`、`POST /auth/password/reset` | 成功后回登录页 |
| 首页 | `GET /food/categories`、`GET /food/list`、`GET /food/recommend`、`GET /user/notices` | 推荐降级不报错 |
| 分类页 / 区域筛选 | `GET /food/categories`、`GET /food/list` | 触底加载 |
| 搜索结果页 | `GET /food/search` | 300ms 防抖 |
| 美食详情页 | `GET /food/{foodId}`、`GET /food/{foodId}/reviews`、`GET /food/{foodId}/share` | 收藏/下单需登录 |
| 写评价弹窗 | `POST /user/upload`、`POST /food/{foodId}/reviews` | 提交后待审核 |
| 评价列表 / 点赞 | `GET /food/{foodId}/reviews`、`POST|DELETE /food/reviews/{reviewId}/like`、`GET /food/reviews/{reviewId}/replies` | 乐观更新 |
| 店铺页 | `GET /merchant/public/{merchantId}`、`GET /food/list?` | 停业提示 |
| 个人中心 | `GET /user/profile`、`GET /user/favorites`、`GET /user/inbox` | 未读数红点 |
| 资料编辑 | `POST /user/upload`、`PUT /user/profile` | 只传改动字段 |
| 隐私与提醒 | `PUT /user/profile`、`GET /user/reminders`、`PUT /user/reminders` | 开关乐观更新 |
| 我的收藏 | `GET /user/favorites`、`DELETE /user/favorites` | 失效项标记 |
| 我的动态 | `GET /user/feeds`、`POST /user/feeds`、`DELETE /user/feeds/{feedId}` | 审核中灰标 |
| 消息中心 | `GET /user/inbox`、`PUT /user/inbox/{messageId}/read`、`PUT /user/inbox/read-all` | 铃铛未读数 |
| 好友列表 | `GET /social/friends` | 按最近消息排序 |
| 加好友 / 新的朋友 | `GET /social/users/search`、`POST /social/friend-requests`、`GET /social/friend-requests/inbox`、`POST /social/friend-requests/{requestId}/handle` | 收件箱 30s 轮询 |
| 聊天窗口 | `POST /social/sessions`、`GET|POST /social/sessions/{sessionId}/messages` | 3–5s 轮询，40301 标红 |
| 黑名单 | `GET /social/blacklist`、`POST /social/blacklist`、`DELETE /social/blacklist/{userId}` | — |
| 拼单列表 | `GET /social/meal-activities` | 已满/结束置灰 |
| 发起拼单 | `POST /social/meal-activities` | 发起人自动占 1 人 |
| 拼单详情 | `GET /social/meal-activities/{activityId}`、`POST .../join`、`POST .../quit` | 已满只禁报名 |
| 管理名单 / 取消 | `GET|DELETE .../members`、`POST .../cancel` | 仅发起人 |
| 活动反馈 | `POST|GET /social/meal-activities/{activityId}/feedback` | 仅 `status=3` |
| 购物车 | `GET /order/cart`、`POST /order/cart`、`PUT /order/cart/{cartItemId}`、`DELETE /order/cart/{cartItemId}` | 失效行灰显 |
| 确认订单页 | `POST /order/checkout/preview`、`POST /order/orders` | 预览失败指明菜品 |
| 收银台 | `POST /order/orders/{orderId}/pay` | 倒计时内支付 |
| 我的订单 | `GET /order/orders`、`GET /order/orders/{orderId}`、`POST /order/orders/{orderId}/cancel` | 详情 10s 轮询 |
| 商家入驻页 | `POST /user/upload`、`POST /merchant/insert`、`GET /merchant/mine` | 驳回可改重提 |
| 店铺设置 | `PUT /merchant/update`、`PUT /merchant/status` | 改店名回待审 |
| 菜品管理 | `GET /merchant/foods/mine`、`POST /merchant/foods/save`、`PUT /merchant/foods/update/{foodId}`、`PUT /merchant/foods/onshelf/{foodId}`、`PUT /merchant/foods/offshelf/{foodId}`、`PUT /merchant/foods/delete/{foodId}` | 有订单不可删 |
| 商家订单台 | `GET /order/merchant/orders`、`POST /order/orders/{orderId}/accept`、`POST /order/orders/{orderId}/complete`、`POST /order/orders/{orderId}/cancel` | 30s 轮询在 **/order** |
| 评价回复 | `GET /merchant/reviews`、`POST /merchant/reviews/{reviewId}/reply` | 已回复改查看 |
| 营销活动 | `GET /merchant/campaigns`、`POST /merchant/campaigns`、`PUT /merchant/campaigns/{campaignId}/offline` | 下线即时生效 |
| 商家统计 | `GET /merchant/stats/daily` | ≤ 90 天 |
| 商家审核 | `GET /admin/merchants/pending`、`PUT /admin/merchants/{merchantId}/approve`、`PUT /admin/merchants/{merchantId}/reject` | 驳回必填原因 |
| 用户管理 | `GET /admin/users`、`GET /admin/users/{userId}`、`POST /admin/users/{userId}/punishments` | 警告不改状态 |
| 内容审核 | `GET /admin/audits`、`PUT /admin/audits/{auditId}` | 拒绝必填意见 |
| 公告管理 | `GET|POST /admin/notices`、`PUT|DELETE /admin/notices/{noticeId}` | 草稿/发布 |
| 推荐位 | `GET /admin/recommend-slots`、`PUT /admin/recommend-slots/{slotId}` | 排序生效于下次刷新 |
| 数据统计 | `GET /admin/stats/overview`、`GET /admin/stats/daily`、`GET /admin/stats/daily/export` | 60s 刷新 |

---

## 6 TypeScript 类型示例

只给前端最常用的 5 个对象；其余按接口 `data` 示例对齐即可。

```ts
/** 统一响应 */
export interface ApiResp<T> {
  code: number;
  message: string;
  data: T | null;
}

/** 分页包 */
export interface PageData<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

/** 1. 登录用户（/auth/login、/auth/register、/user/profile） */
export type Role = 'USER' | 'MERCHANT' | 'ADMIN';

export interface LoginUser {
  id: number;
  nickname: string;
  role: Role;
  roleId: 1 | 2 | 3;
  token: string;
  /** 有效期（秒） */
  expiresIn: number;
}

export interface UserProfile {
  userId: number;
  phone: string | null;
  email: string | null;
  nickname: string;
  avatarUrl: string | null;
  /** 0 未知 1 男 2 女 */
  gender: 0 | 1 | 2;
  school: string | null;
  major: string | null;
  bio: string | null;
  /** 0 不公开 1 公开 */
  privacyFeed: 0 | 1;
  /** 0 不允许陌生人加 1 允许 */
  privacyAddFriend: 0 | 1;
  role: Role;
  roleId: 1 | 2 | 3;
  /** 0 禁用 1 正常（本期无“限制”） */
  status: 0 | 1;
  tasteTags: string | null;
  dislikeIngredients: string | null;
  createdAt: string;
}

/** 2. 美食卡片（/food/list、/food/search、/food/recommend） */
export interface FoodCard {
  foodId: number;
  name: string;
  merchantId: number;
  shopName: string;
  campusArea: string | null;
  categoryId: number | null;
  foodType: string | null;
  /** 元，字符串两位小数 */
  price: string;
  stock: number;
  coverUrl: string | null;
  avgScore: number;
  reviewCount: number;
  salesCount: number;
  recommendTag: string | null;
  /** 1 已上架 */
  status: 0 | 1 | 2;
}

/** 3. 订单（/order/orders、/order/orders/{orderId}） */
export type OrderStatus =
  | 'PENDING_PAY'
  | 'PAID'
  | 'ACCEPTED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface OrderItem {
  foodId: number;
  foodName: string;
  unitPrice: string;
  quantity: number;
  amount: string;
}

export interface Order {
  orderId: number;
  orderNo: string;
  merchantId: number;
  shopName: string;
  items: OrderItem[];
  goodsAmount: string;
  discountAmount: string;
  payAmount: string;
  campaignTitle: string | null;
  /** 1 自提 2 配送 */
  deliveryType: 1 | 2;
  deliveryAddress: string | null;
  remark: string | null;
  status: OrderStatus;
  statusText: string;
  timeline: Array<{ status: OrderStatus; at: string }>;
  cancelReason: string | null;
  createdAt: string;
  /** 仅 PENDING_PAY 有值，用于倒计时 */
  expireAt?: string;
}

/** 4. 聊天消息（/social/sessions/{sessionId}/messages） */
export type MsgType = 'TEXT' | 'IMAGE' | 'EMOJI';

export interface ChatMessage {
  /** 服务端消息 ID，当字符串用 */
  msgId: string;
  sessionId: number;
  fromUserId: number;
  fromNickname: string;
  msgType: MsgType;
  /** TEXT 为文字，IMAGE 为图片 URL，EMOJI 为表情码 */
  content: string;
  createdAt: string;
}

export interface SendMessageReq {
  msgType: MsgType;
  content: string;
  /** 前端生成，用于乐观渲染与去重 */
  clientMsgId?: string;
}

/** 5. 拼单活动（/social/meal-activities） */
export type MealStatus = 0 | 1 | 2 | 3 | 4; // 报名中/已满/进行中/结束/取消

export interface MealActivity {
  activityId: number;
  title: string;
  creatorId: number;
  creatorNickname: string;
  meetTime: string;
  meetPlace: string;
  foodType: string | null;
  maxMembers: number;
  /** 含发起人 */
  currentMembers: number;
  signupDeadline: string | null;
  status: MealStatus;
  joined: boolean;
  isCreator: boolean;
}

export interface MealMember {
  userId: number;
  nickname: string;
  /** 1 发起人 0 成员 */
  role: 0 | 1;
  /** 1 已报名 2 已退出 3 已被移除 */
  status: 1 | 2 | 3;
  joinedAt: string;
}
```

---

## 附录 A 前端必须遵守的三条硬规则

1. **不碰服务端口**：代码里只出现 `http://localhost:8080` 与六个前缀，永远不要写 `:8081`—`:8085`。
2. **不猜金额、不猜状态**：金额与 `statusText` 一律用服务端返回值渲染，不在前端重算或映射。
3. **不按 message 判断**：业务分支只判 `code`；`message` 仅用于展示。

## 附录 B 与既有 `docs/frontend/API.md` 的关系

旧文档是早期实现版（响应体无 `code/message/data` 包装、无订单与拼单能力）。本文档与其**路径保持一致**、响应体升级为统一包装，新增章节为按说明书补全的契约。联调时以本文档为准；旧文档仅作历史参考。
