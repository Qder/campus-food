# Apifox 测试商家接口

默认服务地址：`http://localhost:8080`（未在 `application.yml` 改端口时）。

除 **登录 / 注册** 外，全局 JWT 拦截器要求请求头带 Token。

---

## 一、环境配置

1. Apifox 左侧 **环境管理** → 新建环境，例如 `本地`。
2. 添加变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8080` | 服务根地址 |
| `token` | 留空 | 登录后填入或由脚本自动写入 |

3. 接口 URL 统一写成：`{{baseUrl}}/merchant/...`

---

## 二、认证（先执行）

### 登录 `POST {{baseUrl}}/auth/login`

**Body（JSON）：**

```json
{
  "username": "注册时的手机号或邮箱",
  "password": "密码"
}
```

**成功响应示例：**

```json
{
  "id": 1,
  "nickname": "xxx",
  "roleId": 1,
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

**把 Token 交给后续请求（任选一种）：**

- **手动**：复制 `token` → 环境变量 `token`。
- **自动（推荐）**：在该接口 **后置操作 → 自定义脚本**：

```javascript
const data = pm.response.json();
if (data.token) {
  pm.environment.set("token", data.token);
}
```

（若脚本报错，在 Apifox 帮助里搜「后置脚本 pm.environment」，按你当前版本 API 微调。）

### 全局 Header（环境或项目级）

| 名称 | 值 |
|------|-----|
| `Authorization` | `Bearer {{token}}` |

或在每个需登录接口的 Header 里单独加一行。

---

## 三、商家接口清单

以下均需 **Authorization: Bearer {{token}}**（登录接口除外）。

| 说明 | 方法 | 路径 | Body |
|------|------|------|------|
| 入驻 | POST | `/merchant/insert` | JSON，见下 |
| 更新 | PUT | `/merchant/update` | JSON，必须含 `id` |
| 删除 | PUT | `/merchant/delete/{id}` | 无，路径填店铺 id |
| 我的店 | GET | `/merchant/mine` | 无 |
| 条件查询 | POST | `/merchant/listByCondition` | JSON，见下 |
| 全量列表 | GET | `/merchant/list` | 无 |
| 按 id 查 | GET | `/merchant/get/{id}` | 无 |

### 入驻 Body 示例

```json
{
  "shopName": "测试窗口",
  "campusArea": "一食堂",
  "address": "一楼东侧",
  "contactPhone": "13800138000",
  "description": "Apifox 测试"
}
```

`ownerUserId`、`auditStatus` 不必传，Service 会绑当前用户并设为待审。

### 更新 Body 示例

```json
{
  "id": 1,
  "shopName": "新店名",
  "description": "更新说明"
}
```

仅非 null 字段会更新（MyBatis 动态 SQL）。

### 条件查询 Body 示例

```json
{
  "shopName": "食堂",
  "shopAddress": "",
  "pageNum": 1,
  "pageSize": 10
}
```

字段均可省略；无匹配时返回 `[]`。

---

## 四、推荐测试顺序

1. `POST /auth/login` → 确认 `token` 已写入环境。
2. `POST /merchant/insert` → 返回 `1` 表示成功。
3. `GET /merchant/mine` → 应能看到刚申请的店（`auditStatus` 多为 `0` 待审）。
4. `POST /merchant/listByCondition` → 按店名筛选。
5. `GET /merchant/get/{id}` → 用 mine 返回的 `id`。
6. `PUT /merchant/update` → 改店名。
7. `PUT /merchant/delete/{id}` → 逻辑删除后再 `get` 应为 null 或查不到。

---

## 五、导入 OpenAPI（可选）

- 商家子集：`docs/backend/openapi-merchant.yaml`
- **全站前端联调（推荐）**：`docs/frontend/openapi.yaml`

在 Apifox：**项目设置 → 导入数据 → OpenAPI** 导入后，补上环境变量 `baseUrl`、`token` 即可。

---

## 六、常见错误

| 现象 | 原因 |
|------|------|
| 401 / 未登录 | 未带 `Authorization` 或 token 过期（默认 120 分钟） |
| 无权操作该店铺 | 更新/删除的 `id` 不是当前用户的店 |
| 更新失败 / 字段全 null | `PUT /update` 未选 JSON Body 或未传 `id` |
| 删除无效 | 路径应为 `/merchant/delete/1`，不是 query 里漏绑 id |
