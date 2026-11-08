# campus-food

校园美食智能推荐与社交平台（Spring Cloud 微服务 + MyBatis + Redis + MongoDB）。

## 文档

- **[docs/README.md](./docs/README.md)**：文档总览  
- **前端联调**：[docs/frontend/README.md](./docs/frontend/README.md)、[API 说明](./docs/frontend/API.md)、[V1.3 契约](./docs/frontend/API-frontend-v1.3.md)  
- **后端笔记**：[docs/backend/](./docs/backend/)

## 模块与端口

| 模块 | 启动类 | 端口 | 说明 |
|------|--------|------|------|
| `eureka-server` | `com.campus.food.eureka.EurekaServerApplication` | **8761** | 注册中心，控制台 `http://localhost:8761` |
| `user-service` | `com.campus.food.user.UserServiceApplication` | **8081** | 登录、资料、收藏/提醒/动态；`/internal/**` 仅服务间 Feign |
| `food-service` | `com.campus.food.food.FoodServiceApplication` | **8082** | 美食浏览、评价、推荐（Redis：热门/搜索/浏览历史） |
| `merchant-service` | `com.campus.food.merchant.MerchantServiceApplication` | **8083** | 商家入驻、菜品、管理端审核 |
| `social-service` | `com.campus.food.social.SocialServiceApplication` | **8084** | 好友、拉黑、私聊（Mongo）、拼单约饭 |
| `order-service` | `com.campus.food.order.OrderServiceApplication` | **8085** | 下单支付、用户查单、商家接单/拒单/完成 |
| `api-gateway` | `com.campus.food.gateway.ApiGatewayApplication` | **8080** | **对外统一入口**（前端 / Apifox 默认打这个） |

父工程为 Maven 多模块，根目录执行 `./mvnw`（Windows：`mvnw.cmd`）或在 IDE 中分别运行上表启动类。

网关按路径转发：`/auth`、`/user` → user；`/food` → food；`/merchant`、`/admin` → merchant；`/social` → social；`/order` → order。

## 本地运行

### 1. 环境

- JDK 17  
- MySQL：库名 `campus_food`，执行根目录 [建表语句.sql](./建表语句.sql) 及 `sql/` 下补丁/种子（按需）  
- Redis：`localhost:6379`（food 列表缓存与浏览历史；social 好友关系；order 下单幂等。未启动时订单幂等会降级，其它依赖 Redis 的接口可能失败）  
- MongoDB：`mongodb://localhost:27017/campus_food_chat`（私聊消息正文；会话元数据仍在 MySQL）

### 2. 配置

各服务数据库账号在各自 `application.yml` 中修改（默认各业务服务均指向 `jdbc:mysql://localhost:3306/campus_food`）。

### 3. 启动顺序

建议按顺序启动，并在 Eureka 控制台确认实例为 **UP**：

1. **Eureka**（8761）  
2. **user-service**（8081）、**food-service**（8082）、**merchant-service**（8083）、**social-service**（8084）、**order-service**（8085）  
3. **api-gateway**（8080）

### 4. 联调地址

- **浏览器 / 前端 / Apifox 业务接口**：`http://localhost:8080`  
  - 例如：`POST /auth/login`、`GET /food/recommend`、`POST /order/orders`（路径由 Gateway 转发，见 `api-gateway/src/main/resources/application.yml`）  
- **Eureka**：`http://localhost:8761`  
- **Feign 内部接口**（不要走 8080）：`http://localhost:8081/internal/...`，仅供微服务间调用  

更细的鉴权与接口列表见 [docs/frontend/API.md](./docs/frontend/API.md)。订单商家接口走 `/order/merchant/**`，不要打 `/merchant/**`。
