# 前端开发说明

给**前端同事**看。不用读 Java、不用进各微服务目录。

**接口唯一来源：** [校园美食平台完整接口文档.md](../校园美食平台完整接口文档.md)

每条接口都有：路径、要不要登录、请求 JSON、返回 JSON、标题上的 **已实现 / 还没做**。字段名按文档原样发、原样收。

本目录另外两份可拷进前端工程：

| 文件 | 用途 |
|------|------|
| [types.ts](./types.ts) | 和文档同一套 TypeScript 类型 |
| [http-client.example.ts](./http-client.example.ts) | Axios：带 Token、处理 401 |

类型和文档打架时，**以接口文档为准**。

---

## 环境

| 项 | 值 |
|----|-----|
| Base URL | `http://localhost:8080`（只打网关，不要写 8081–8085） |
| Content-Type | `application/json`（有 Body 时） |
| 鉴权 | `Authorization: Bearer <token>` |
| 联调 | 后端同事把网关开着；同一局域网把 `localhost` 换成他的 IP |

Vite 代理（代码里仍写 `/auth/login`，请求走 `/api`）：

```ts
// vite.config.ts
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

---

## 成功 / 失败（不要自己再包一层）

| HTTP | body | 前端怎么做 |
|------|------|------------|
| 200 | **就是业务 JSON**，没有 `{ "code": 0, "data": ... }` | `const { data } = await http.get(...)`，用 `data`，不要写 `data.data` |
| 200 + 空 body | 商家接单/完成等 `void` 接口 | 当成功 |
| 200 + 数字 | 商家 `insert/update/delete` | 影响行数，一般是 `1` |
| 400 | `{ "code": 400, "message": "原因" }` | 把 `message` 弹给用户 |
| 401 | `{ "code": 401, "message": "..." }` | 清 token，跳登录 |
| 500 | | 提示「服务繁忙，请稍后重试」 |

分页（页码从 **1** 开始）：`{ total, page, size, list }`。空列表是 `list: []`，不是 404。

`roleId`：`1` 学生　`2` 商家　`3` 管理员。登录后按 `roleId` 进三个端，不要前端猜。

金额是**数字**（`18.0`）。时间是 `"2026-11-10T21:00:00"` 这类。用户资料主键叫 `id`，不是 `userId`。

---

## 三个端

| 端 | 条件 | 主要页面 |
|----|------|----------|
| 学生 | `roleId === 1` | 浏览/搜索/详情、推荐、下单、我的订单、评价、好友聊天、拼单、个人中心 |
| 商家 | `roleId === 2` | 入驻/我的店、菜品上下架、商家订单（接单/拒单/完成） |
| 管理 | `roleId === 3` | 审店（待审/通过/驳回）、用户列表；封禁、公告、报表文档标了还没做 |

注册出来一定是学生。商家要等入驻审核通过后 `roleId` 才会变成 2。

---

## 按模块：先做已实现的

下面只列**现网已通**的路径。标了还没做的（验证码、上传、轮播、群聊、营销、管理报表等）先不要调，会 404。细节仍看完整接口文档对应章节。

### 登录（文档第 1 章）

`POST /auth/register`　`POST /auth/login`（不用 Token）

登录返回 `token`、`id`、`nickname`、`roleId`，存本地。没有退出接口，清本地即可。

### 个人中心（第 2 章）

`GET/PUT /user/profile`  
`GET/POST/DELETE /user/favorites`  
`GET/PUT /user/reminders`  
`GET/POST /user/feeds`　`DELETE /user/feeds/{id}`

### 美食（第 3 章）

不用登录：`GET /food/categories` `/food/list` `/food/search` `/food/{id}` `/food/{id}/reviews`  
要登录：`GET /food/recommend`　`POST /food/{id}/reviews`

推荐不强制口味；未登录是 401，不会降级成热门。

### 好友 / 私聊（第 4 章）

路径按现网名字，不要改成 REST 新路径：

`POST /social/sendFriendRequest`　`POST /social/handleFriendRequest`  
`GET /social/friends`　`GET /social/friend-requests/inbox`　`POST /social/friends/{id}/delete`  
`POST /social/sessions`（现在只能 `sessionType: 1` 私聊）  
`GET/POST /social/sessions/{id}/messages`  
`POST /social/blacklist`　`DELETE /social/blacklist/{userId}`　`GET /social/blacklist`

### 拼单约饭（第 5 章）

`GET /social/meal-activities`　`POST /social/meal-activities`  
`GET /social/meal-activities/{id}`  
`POST .../join` `.../quit` `.../cancel` `.../feedback`  
`GET .../members`　`DELETE .../members/{userId}`　`GET .../feedback`

列表、详情可不登录；报名、发起要登录。

### 商家（第 6 章）

用户端：`GET /merchant/list`　`GET /merchant/get/{id}`（可不登录）  
店主：`POST /merchant/insert`　`PUT /merchant/update`　`PUT /merchant/delete/{id}`　`GET /merchant/mine`  
菜品：`POST /merchant/foods/save`　`PUT .../update/{id}`　`PUT .../delete|onshelf|offshelf/{id}`　`GET /merchant/foods/mine` 等

建店/改店成功体是**数字**，不是店铺对象。新增菜品成功体是新菜 **id 数字**。

### 订单（第 7 章）

没有购物车、没有单独收银台。详情页直接：

`POST /order/orders`（body 用 `items`，模拟支付后直接 `PAID`）  
`GET /order/orders`　`GET /order/orders/{orderId}`  
商家：`GET /order/merchant/orders`  
`POST /order/merchant/orders/{id}/accept|cancel|complete`（成功空 body）

不要打 `/merchant` 去接单。

### 管理（第 8 章）

已实现：`GET /admin/merchants/pending`　`POST /admin/merchants`　`PUT .../approve`　`PUT .../reject`  
`GET /admin/users`（`keyword` `status` `page` `size` 均可省略）  
要 `roleId === 3`。封禁、公告、报表文档标了还没做。

---

## 不要做这些

- 不要把响应改成 `{ code, message, data }`
- 不要把 `/social/sendFriendRequest` 改成 `/social/friend-requests`
- 不要做购物车、结算预览、独立支付
- 金额不要改成字符串
- 文档标题 **还没做** 的接口先做页面占位即可
- 识图、分享到微信：课设不要求接口；分享可复制详情链接

课设明确砍掉的能力见接口文档文末。

---

## 建议页面（对接已实现接口）

| 页面 | 调这些 |
|------|--------|
| 登录 / 注册 | `/auth/login` `/auth/register` |
| 首页 | `/food/categories` `/food/list` `/food/recommend`（登录后） |
| 搜索 | `/food/search` |
| 美食详情 | `/food/{id}` `/food/{id}/reviews`，登录后可评价、下单 |
| 下单 | `POST /order/orders` |
| 我的订单 | `GET /order/orders` `/order/orders/{id}` |
| 个人中心 | profile、收藏、动态、提醒 |
| 好友 / 聊天 | 第 4 章全部已实现项 |
| 拼单 | 第 5 章 |
| 商家后台 | 第 6、7 章店主接口 |
| 管理后台 | 第 8 章审店 |

---

## 联调检查

1. 登录成功，本地有 `token`，后续请求带 `Bearer`。
2. `GET /food/list` 能出列表（可未登录）。
3. 带 token 调 `GET /food/recommend` 返回数组。
4. 学生号下单 `POST /order/orders` 返回 `status: "PAID"`。
5. HTTP 400 时页面上看到的是后端 `message`，不是自己拼的英文。

对不上字段时，用 Apifox 打同一路径，以实际 JSON 为准，回来改文档；不要翻后端代码。
