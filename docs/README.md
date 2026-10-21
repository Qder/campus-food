# campus-food 项目文档

文档按读者拆分：**前端联调**看 `frontend/`，**后端实现与课设笔记**看 `backend/`。

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
| [backend/推荐功能笔记.md](./backend/推荐功能笔记.md) | 推荐打分链路（对照源码） |
| [backend/开发商店步骤.md](./backend/开发商店步骤.md) | 商家模块开发步骤（课设） |
| [backend/apifox-merchant.md](./backend/apifox-merchant.md) | 商家接口 Apifox 测试说明 |
| [backend/merchant-api-test.http](./backend/merchant-api-test.http) | REST Client 用例 |
| [backend/openapi-merchant.yaml](./backend/openapi-merchant.yaml) | 商家 + 登录 OpenAPI（旧版子集） |

## 本地默认地址

- 后端：`http://localhost:8080`（未改 `application.yml` 端口时）
- 数据库：`campus_food`（见 `src/main/resources/application.yml`）
