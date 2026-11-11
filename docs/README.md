# campus-food 项目文档

| 文档 | 说明 |
|------|------|
| [课程设计任务书.md](./课程设计任务书.md) | 课设功能范围 |
| **[校园美食平台完整接口文档.md](./校园美食平台完整接口文档.md)** | **前后端共用接口：路径 + 请求 JSON + 返回 JSON** |

## 前端（交给前端同事，不用读后端代码）

入口：[frontend/README.md](./frontend/README.md)

| 文档 | 说明 |
|------|------|
| [校园美食平台完整接口文档.md](./校园美食平台完整接口文档.md) | **唯一接口契约** |
| [frontend/README.md](./frontend/README.md) | 环境、三端、已实现路径、页面对照 |
| [frontend/types.ts](./frontend/types.ts) | 可拷进前端项目的类型 |
| [frontend/http-client.example.ts](./frontend/http-client.example.ts) | Axios 示例 |

## 后端

| 文档 | 说明 |
|------|------|
| [backend/JWT鉴权笔记.md](./backend/JWT鉴权笔记.md) | Token 签发与拦截器流程 |
| [backend/Eureka注册与发现笔记.md](./backend/Eureka注册与发现笔记.md) | Eureka Server/Client、注册流程、Feign/Gateway 如何用服务名 |
| [backend/推荐功能笔记.md](./backend/推荐功能笔记.md) | 推荐打分链路（对照源码） |
| [backend/开发商店步骤.md](./backend/开发商店步骤.md) | 商家模块开发步骤（课设） |
| [backend/apifox-merchant.md](./backend/apifox-merchant.md) | 商家接口 Apifox 测试说明 |
| [backend/merchant-api-test.http](./backend/merchant-api-test.http) | REST Client 用例 |
| [backend/openapi-merchant.yaml](./backend/openapi-merchant.yaml) | 商家 + 登录 OpenAPI（旧版子集） |

## 本地默认地址

- 对外 API（Gateway）：`http://localhost:8080`
- Eureka：`http://localhost:8761`
- 各微服务直连：user **8081**、food **8082**、merchant **8083**、social **8084**（详见仓库根 [README.md](../README.md)）
- 数据库：`campus_food`（见各服务 `*/src/main/resources/application.yml`）
