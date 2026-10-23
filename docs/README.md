# campus-food 项目文档

文档按读者拆分：**课设任务书**看仓库根下本目录的任务书，**前端联调**看 `frontend/`，**后端实现与课设笔记**看 `backend/`。

| 文档 | 说明 |
|------|------|
| [课程设计任务书.md](./课程设计任务书.md) | 按《（课程设计要求）校园美食智能推荐与社交平台》改写的正式任务书 |

## 前端（联调、页面开发）

| 文档 | 说明 |
|------|------|
| [frontend/README.md](./frontend/README.md) | 快速上手：环境、鉴权、错误格式、页面与接口对照 |
| [frontend/API.md](./frontend/API.md) | 全量 HTTP 接口说明（路径、参数、响应、是否需登录） |
| [frontend/types.ts](./frontend/types.ts) | 与后端 VO/DTO 对齐的 TypeScript 类型 |
| [frontend/http-client.example.ts](./frontend/http-client.example.ts) | Axios 封装示例（Token、401 处理） |
| [frontend/openapi.yaml](./frontend/openapi.yaml) | OpenAPI 3，可导入 Apifox / Postman |

## 后端（实现、测试、答辩）

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
