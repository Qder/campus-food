# 前端联调指南

面向 Vue / React / 小程序等调用本后端时的约定说明。接口明细见 [API.md](./API.md)，类型见 [types.ts](./types.ts)。

## 1. 基础配置

| 项 | 值 |
|----|-----|
| Base URL | `http://localhost:8080` |
| Content-Type | `application/json`（有 Body 的接口） |
| 鉴权头 | `Authorization: Bearer <token>` |
| Token 有效期 | 120 分钟（`jwt.expire-minutes`） |

开发时若浏览器跨域报错，请在前端 devServer 配置 **代理**（推荐），或在后端增加 CORS（当前仓库未默认开启 CORS）。

**Vite 代理示例**（`vite.config.ts`）：

```ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
});
```

前端请求统一写成 `fetch('/api/food/list')`，避免直连 8080 的跨域问题。

## 2. 鉴权与公开接口

### 2.1 无需 Token

- `POST /auth/register`
- `POST /auth/login`
- 以下 **GET**（美食浏览，拦截器白名单）：
  - `/food/categories`
  - `/food/list`
  - `/food/search`
  - `/food/{id}`（数字 id）
  - `/food/{id}/reviews`

### 2.2 必须 Token

除上述路径外，**所有接口**（含 `GET /food/recommend`、`POST /food/{id}/reviews`、用户资料、商家、管理员、店主菜品）都要带 `Authorization`。

管理员接口（`/admin/**`）还要求用户 **`roleId === 3`**，否则业务层返回 400「无管理员权限」。

### 2.3 登录后建议存什么

`POST /auth/login` 成功体（`LoginVO`）：

```json
{
  "id": 1,
  "nickname": "小明",
  "roleId": 1,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

前端通常持久化：`token`、`id`、`nickname`、`roleId`。后续请求只带 `token` 即可；**不要**把 `userId` 放在 query/body 里冒充当前用户，后端以 Token 为准。

### 2.4 角色与页面

| roleId | 含义 | 典型页面 |
|--------|------|----------|
| 1 | 普通用户 | 首页美食、推荐、评价、个人资料 |
| 2 | 商家 | 我的店铺、菜品维护（入驻后由管理员审核通过并升角色） |
| 3 | 管理员 | 待审店铺、通过/驳回、代建店铺 |

## 3. 响应与错误格式

### 3.1 成功响应

**没有统一 `{ code, data }` 包装**。HTTP 200 时 body 直接是业务对象或数组，例如：

- 登录 → `LoginVO`
- 美食列表 → `PageVO<FoodItemVO>`
- 分类 → `FoodCategoryVO[]`

分页结构 `PageVO`：

```json
{
  "total": 100,
  "page": 1,
  "size": 10,
  "list": [ /* ... */ ]
}
```

`page` 从 **1** 开始；`size` 默认 10，美食相关接口最大 **50**。

### 3.2 错误响应

| HTTP | 场景 | Body 示例 |
|------|------|-----------|
| 401 | 未带 Token / Token 无效 / 过期 | `{"code":401,"message":"未登录或 Token 缺失"}` |
| 400 | 业务异常 `BizException`、参数校验失败 | `{"code":400,"message":"具体原因"}` |
| 500 | 未捕获异常 | `{"code":500,"message":"服务器内部错误"}` |

401 由 **JWT 拦截器**直接写出，不会进入全局异常处理器。前端建议在响应拦截器里：`code === 401` 时清空 Token 并跳转登录页。

## 4. 推荐功能（前端必知）

1. **必须先登录**再调 `GET /food/recommend`。
2. 用户须在资料里设置 **口味** `tasteTags`（逗号分隔，如 `"辣,咸鲜"`），否则返回 400「请先完善口味偏好」。注册接口**不要求**口味，可在注册后调 `PUT /user/profile` 写入。
3. 可选 **忌口** `dislikeIngredients`，推荐结果会过滤含这些食材的菜品。
4. 推荐项结构：

```json
{
  "score": 0.59,
  "recommendTags": "辣",
  "food": { /* FoodItemVO，不含 tasteTags/ingredients */ }
}
```

## 5. 商家与店铺状态（展示用）

**店铺审核 `auditStatus`**

| 值 | 说明 |
|----|------|
| 0 | 待审 |
| 1 | 通过 |
| 2 | 驳回 |
| 3 | 取消资格 |

**店铺经营 `status`**

| 值 | 说明 |
|----|------|
| 0 | 未开业 |
| 1 | 营业 |
| 2 | 停业 |

`GET /merchant/list` 仅返回 **审核通过且营业中** 的店，适合做用户端店铺列表。店主看自己所有店用 `GET /merchant/mine`（含待审）。

## 6. 建议的页面 → 接口映射

| 页面 | 主要接口 |
|------|----------|
| 注册 / 登录 | `POST /auth/register`、`POST /auth/login` |
| 个人中心 | `GET/PUT /user/profile` |
| 首页分类 | `GET /food/categories` |
| 美食列表 / 筛选 | `GET /food/list?categoryId=&campusArea=&page=&size=` |
| 搜索 | `GET /food/search?keyword=&page=&size=` |
| 美食详情 | `GET /food/{id}`、`GET /food/{id}/reviews` |
| 写评价 | `POST /food/{id}/reviews`（需登录） |
| 个性化推荐 | `GET /food/recommend?limit=`（需登录 + 口味） |
| 商家入驻 | `POST /merchant/insert` |
| 我的店铺 | `GET /merchant/mine` |
| 编辑店铺 | `PUT /merchant/update`（body 必含 `id`） |
| 店主菜品 | `POST /merchant/foods/save`、`PUT /merchant/foods/update/{id}`、`PUT /merchant/foods/delete/{id}` |
| 管理待审 | `GET /admin/merchants/pending` |
| 审核通过/驳回 | `PUT /admin/merchants/{id}/approve`、`PUT /admin/merchants/{id}/reject` |

## 7. 联调顺序（第一次跑通）

1. 启动 MySQL + 后端 `CampusFoodApplication`。
2. `POST /auth/register` 注册用户。
3. `PUT /user/profile` 设置 `tasteTags`（要做推荐时）。
4. `POST /auth/login` → 保存 `token`。
5. `GET /food/categories`、`GET /food/list`（可不登录）。
6. 带 Token：`GET /user/profile`、`GET /food/recommend`。
7. 商家流：`POST /merchant/insert` → `GET /merchant/mine`（管理员账号审核后再上架菜品）。

更完整的请求体与字段说明见 [API.md](./API.md)。可复制 [http-client.example.ts](./http-client.example.ts) 作为项目 HTTP 层起点。
