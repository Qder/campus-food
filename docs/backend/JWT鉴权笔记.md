# JWT 鉴权笔记（campus-food）

> 对照当前代码：登录签发 Token，拦截器校验，Controller 从 `UserContext` 取当前用户。

---

## 1. JWT 是什么

JWT（JSON Web Token）是一段**可校验的字符串**，用来证明「这个请求是哪个用户发的」。

结构：`Header.Payload.Signature`（三段 Base64，用点连接）

| 段 | 作用 |
|---|---|
| Header | 算法，如 HS256 |
| Payload | 自定义数据，本项目放 `userId`、`roleId` |
| Signature | 用 `secret` 签名，防止被改 |

服务端**不存 Session**。每次请求带上 Token，服务端用同一把 `secret` 验签即可。

请求头格式：

```http
Authorization: Bearer <token>
```

---

## 2. 本项目里的文件

| 文件 | 职责 |
|---|---|
| `application.yml` 的 `jwt.*` | secret、过期时间、请求头名 |
| `JwtProperties` | 把 yml 绑成 Java 对象 |
| `JwtUtil` | 签发、解析、校验 Token |
| `AuthServiceImpl.login` | 密码正确后调用 `generateToken` |
| `WebMvcConfig` | 注册拦截器，放行登录/注册 |
| `JwtAuthInterceptor` | 取 Token → 校验 → 写入上下文 |
| `UserContext` | ThreadLocal 存当前 `userId` / `roleId` |

依赖：`jjwt-api` / `jjwt-impl` / `jjwt-jackson`（0.12.x）。

配置（`application.yml`）：

```yaml
jwt:
  secret: "至少32字符的随机串"   # HS256 要求密钥够长
  expire-minutes: 120            # access 有效期 2 小时
  header: "Authorization"
  prefix: "Bearer "
```

---

## 3. 整体流程

```
注册/登录（匿名）
    │
    ▼
POST /auth/login
    │  查用户 + BCrypt.matches
    ▼
JwtUtil.generateToken(userId, roleId)
    │  返回 LoginVO.token
    ▼
前端保存 token，之后每个请求带 Header
    │
    ▼
JwtAuthInterceptor.preHandle
    │  无 token / 无效 / 过期 → 401，进不了 Controller
    │  有效 → UserContext.set(userId, roleId)
    ▼
Controller / Service
    │  UserContext.getUserId() 拿当前用户（不要让前端传 userId）
    ▼
afterCompletion → UserContext.clear()
```

**放行路径**（不校验 Token）：`/auth/register`、`/auth/login`、`/error`。  
其它 `/**` 都要带 Token。

---

## 4. 签发（登录成功）

位置：`AuthServiceImpl.login`

1. `username` 含 `@` 按邮箱查，否则按手机号查。
2. `encoder.matches(明文, passwordHash)`。
3. `status == 1` 才允许登录。
4. `jwtUtil.generateToken(user.getId(), user.getRoleId())` 写入 `LoginVO.token`。

`generateToken` 写入的内容：

- claim `userId`、`roleId`
- `sub` = userId 字符串
- `iat` = 签发时间
- `exp` = 当前时间 + `expire-minutes` 分钟
- `signWith(HMAC-SHA256(secret))`

Payload 里**不要**放密码、手机号等敏感信息。

---

## 5. 校验（拦截器）

位置：`JwtAuthInterceptor.preHandle`

1. `OPTIONS` 直接放行（CORS 预检）。
2. 读 Header：`jwtUtil.getHeaderName()`（Authorization）。
3. `resolveToken`：去掉 `Bearer ` 前缀。
4. `validateToken`：验签 + 未过期。
5. `getUserId` / `getRoleId` 写入 `UserContext`。
6. `preHandle` 返回 `false` 时自己写 JSON 401，**不会**进 Controller，也**不会**走 `GlobalExceptionHandler`。

业务接口里取当前用户：

```java
Long userId = UserContext.getUserId();
```

---

## 6. 为什么用 ThreadLocal（UserContext）

一次请求可能在线程池里复用同一条线程。

- `set`：本请求绑定 userId
- Controller 里 `getUserId`
- `afterCompletion` 里必须 `clear()`，否则下一个请求可能读到上一个用户

不要用静态变量存「当前用户」。

---

## 7. 联调示例

登录：

```http
POST /auth/login
Content-Type: application/json

{ "username": "13800138000", "password": "yourPassword" }
```

成功体里有 `token`。之后：

```http
GET /user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

没有 Header 或 Token 坏了 → `{ "code": 401, "message": "..." }`。

---

## 8. 答辩可说的点

- 无状态：服务端不存登录 Session，Token 自带身份，适合 REST。
- 防篡改：改 payload 后签名对不上。
- 过期：`exp` 到期必须重新登录（本课设暂无 refresh）。
- 越权：接口用 `UserContext.getUserId()`，禁止用请求体里的 userId 当「当前用户」。
- 密钥：`secret` 不能提交到公开仓库；本地课设可写在 yml，上线用环境变量。

---

## 9. 本课设还没做（知道即可）

- refresh token（文档写 access 2h + refresh 7d）
- 退出时 Redis 黑名单
- Gateway 统一鉴权
- 注册成功后立刻签发 Token
- `sys_login_log`、更新 `last_login_at`

当前最小闭环：**登录拿 Token → 拦截器校验 → UserContext 给业务用**。
