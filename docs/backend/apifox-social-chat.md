# Apifox 测试私聊接口

默认走网关：`http://localhost:8080`（不要直连 social-service 的 8084）。

聊天三条都要 JWT。私聊还要求**对方已是好友**，所以要准备 **两个账号**，先加好友再聊。

本地需已启动：Eureka、Gateway、user-service、social-service，以及 MySQL `campus_food`、MongoDB（`27017`，库会自动出 `campus_food_chat`）、Redis。

---

## 一、环境配置

1. Apifox 左侧 **环境管理** → 可用现成的 `本地`，或新建。
2. 变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8080` | 网关 |
| `token` | 留空 | 当前正在用的 JWT（测哪边就换成哪边） |
| `tokenA` / `tokenB` | 留空 | 两个用户各自登录后的 token |
| `userIdA` / `userIdB` | 留空 | 登录返回的 `id` |
| `sessionId` | 留空 | 创建会话后填 |
| `beforeMsgId` | 留空 | 上滑时填当前页最早一条的 `msgId` |

3. 接口 URL 写成：`{{baseUrl}}/social/...`

### 全局 Header

| 名称 | 值 |
|------|-----|
| `Authorization` | `Bearer {{token}}` |
| `Content-Type` | `application/json`（有 Body 的接口） |

测用户 A 时把 `token` 改成 `{{tokenA}}` 的值（或后置脚本 `pm.environment.set("token", pm.environment.get("tokenA"))`）。测用户 B 同理。

---

## 二、先拿到两个登录态

需要两个已注册用户（手机号/邮箱 + 密码）。没有就先 `POST {{baseUrl}}/auth/register`。

### 1. 用户 A 登录 `POST {{baseUrl}}/auth/login`

Body：

```json
{
  "username": "A的手机号或邮箱",
  "password": "密码"
}
```

成功体（未包一层 `data`）：

```json
{
  "id": 1001,
  "nickname": "小杨",
  "roleId": 1,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

后置脚本示例：

```javascript
const data = pm.response.json();
if (data.token) {
  pm.environment.set("tokenA", data.token);
  pm.environment.set("userIdA", String(data.id));
  pm.environment.set("token", data.token);
}
```

### 2. 用户 B 登录

同一接口，换成 B 的账号。后置脚本把 `tokenB`、`userIdB` 写上，**不要**覆盖 `tokenA`。测 B 的接口时再把 `token` 设为 `tokenB`。

Apifox 里也可以建两个目录「用户A / 用户B」，Header 分别写 `Bearer {{tokenA}}`、`Bearer {{tokenB}}`，更省事。

---

## 三、先加好友（否则建会话会 400「请先添加对方为好友」）

实际路径以 **Controller** 为准（不是前端文档里的 `/social/friend-requests`）。

### 1. A 申请加 B  `POST {{baseUrl}}/social/sendFriendRequest`

Header：`Authorization: Bearer {{tokenA}}`

```json
{
  "toUserId": 1002,
  "message": "交个朋友"
}
```

`toUserId` 填环境变量 `userIdB`（数字，不要加引号）。记下返回的 `id`（申请单号）。

### 2. B 看收件箱  `GET {{baseUrl}}/social/friend-requests/inbox`

Header：Bearer `tokenB`。应能看到刚才那条，`status` 为 `0`。

### 3. B 同意  `POST {{baseUrl}}/social/handleFriendRequest`

Header：Bearer `tokenB`

```json
{
  "id": 1,
  "status": 1
}
```

`id` 是申请单号，`status`：`1` 同意 / `2` 拒绝。

### 4. 确认  `GET {{baseUrl}}/social/friends`

A、B 两边都打一次，列表里应出现对方。`FriendVo` 里的 `id` 就是后面的 `peerUserId`。

---

## 四、私聊三条（核心）

### 1. 创建或返回会话  `POST {{baseUrl}}/social/sessions`

Header：Bearer `tokenA`  
Body **必须 JSON**：

```json
{
  "peerUserId": 1002,
  "sessionType": 1
}
```

成功（HTTP 200，body 就是 VO，没有 `code/data` 信封）：

```json
{
  "sessionId": 5001,
  "sessionType": 1,
  "peerUserId": 1002,
  "peerNickname": "对方昵称",
  "peerAvatarUrl": "..."
}
```

把 `sessionId` 写入环境变量。再发一次同样 Body，应返回**同一个** `sessionId`。

B 调同一接口、`peerUserId` 填 A 的 id，也应是同一场。

常见 400（body 为 `{ "code": 400, "message": "..." }`）：

| message | 原因 |
|---------|------|
| 请先登录 | 没带 Token 或过期 |
| 本期仅支持私聊 | `sessionType` 不是 `1` |
| 不能与自己发起会话 | `peerUserId` 填成了自己 |
| 请先添加对方为好友 | 没走完第三节 |
| 你已被对方拉黑，无法发起会话 | `user_blacklist` 里有对方拉黑你 |

### 2. 发消息  `POST {{baseUrl}}/social/sessions/{{sessionId}}/messages`

路径里的 `sessionId` 用上一步的数字。Header：Bearer `tokenA`

文字：

```json
{
  "msgType": "TEXT",
  "content": "明天一起去？",
  "clientMsgId": "c-1"
}
```

图片（`content` 填已有的 URL，本接口不上传文件）：

```json
{
  "msgType": "IMAGE",
  "content": "https://example.com/chat/1.jpg"
}
```

表情：`"msgType": "EMOJI"`，`content` 填表情码。

成功示例：

```json
{
  "msgId": "665f2a9c1b2e4d0012ab34cd",
  "clientMsgId": "c-1",
  "sessionId": 5001,
  "fromUserId": 1001,
  "fromNickname": "我",
  "msgType": "TEXT",
  "content": "明天一起去？",
  "createdAt": "2026-10-26T19:40:00"
}
```

`msgId` 是 **Mongo `_id` 字符串**，后面上滑当 `beforeMsgId` 用。可换 `tokenB` 再发几条，模拟两人对话。

### 3. 拉历史  `GET {{baseUrl}}/social/sessions/{{sessionId}}/messages`

Query（均可不传）：

| 参数 | 说明 |
|------|------|
| `size` | 默认 20，最大 50 |
| `beforeMsgId` | 不传 = 最新一页；传入当前页**最早**一条的 `msgId` = 再往更早翻 |

例：

- 进入聊天页：`GET {{baseUrl}}/social/sessions/5001/messages?size=20`
- 上滑：`GET .../messages?beforeMsgId=665f2a9c1b2e4d0012ab34cd&size=20`

成功：

```json
{
  "sessionId": 5001,
  "hasMore": false,
  "list": [ ]
}
```

`list` **时间倒序**（新的在前）。`hasMore == true` 说明还能再滑。不是成员会 400「无权访问该会话」。

---

## 五、推荐点击顺序

1. A 登录 → 存 `tokenA`、`userIdA`
2. B 登录 → 存 `tokenB`、`userIdB`
3. A `sendFriendRequest`（`toUserId` = B）
4. B `inbox` → `handleFriendRequest`（`status: 1`）
5. A `POST /social/sessions` → 存 `sessionId`
6. A `POST .../messages` 发 TEXT
7. B 换 Token，同一 `sessionId` 再发一条
8. A `GET .../messages` 应看到两条，自己的 `fromNickname` 为「我」
9. 再发十几条后 `size=5`，把返回 list **最后一条**（最早）的 `msgId` 填进 `beforeMsgId`，再 GET，应是更早的一页

库里核对（可选）：MySQL `chat_session` / `chat_session_member` / `chat_message_index`；Mongo 库 `campus_food_chat` 集合 `chat_message`。

### 10. 黑名单（可选，接在互为好友之后）

`POST {{baseUrl}}/social/blacklist` Header：Bearer `tokenA`

```json
{ "userId": 1002 }
```

成功：`{ "userId": 1002, "blocked": true }`。`GET /social/friends` 不应再有对方。B 再 `POST .../messages` 应为 400「消息发送失败，对方已将你拉黑」。

`GET {{baseUrl}}/social/blacklist` 看名单。`DELETE {{baseUrl}}/social/blacklist/1002` 移出后可重新发好友申请，**不会**自动变回好友。

---

## 六、Apifox 里注意

- Body 选 **JSON**，不要 form-data。
- `peerUserId`、`sessionType`、`toUserId` 用**数字**。
- `beforeMsgId` 是字符串，Query 里直接粘 `msgId`。
- 成功接口往往是 **直接返回 VO**；失败才是 HTTP **400** + `{code, message}`。不要按前端文档里的 `code: 0` 信封去断言。
- 不要用 `ChatPingController` 当业务接口；那只是探 Mongo。
