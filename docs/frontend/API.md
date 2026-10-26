# HTTP 接口说明（前端版）

Base URL：`http://localhost:8080`（或通过前端代理的 `/api` 前缀）。

除特别说明外，成功均为 **HTTP 200 + 直接 JSON  body**（无 `data` 外层）。

---

## 认证

### POST `/auth/register`

无需 Token。

**Body `RegisterDTO`**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 二选一 | 手机号；可与 email 同时填 |
| email | string | 二选一 | 邮箱 |
| password | string | 是 | 6~32 位，不能含空格 |
| nickname | string | 是 | 最长 20 字 |

**响应 `RegisterVO`**

| 字段 | 类型 |
|------|------|
| id | number |
| nickname | string |
| roleId | number |

---

### POST `/auth/login`

无需 Token。

**Body `LoginDTO`**

| 字段 | 类型 | 必填 |
|------|------|------|
| username | string | 是（手机号或邮箱） |
| password | string | 是 |

**响应 `LoginVO`**

| 字段 | 类型 |
|------|------|
| id | number |
| nickname | string |
| roleId | number |
| token | string |

---

## 用户资料（需 Token）

### GET `/user/profile`

**响应 `UserProfileVO`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | |
| phone | string \| null | |
| email | string \| null | |
| nickname | string | |
| avatarUrl | string \| null | |
| gender | number | 0 未知 1 男 2 女 |
| school | string \| null | |
| major | string \| null | |
| bio | string \| null | |
| privacyFeed | number | 0/1 |
| privacyAddFriend | number | 0/1 |
| roleId | number | |
| status | number | 0 禁用 1 正常 |
| tasteTags | string \| null | 逗号分隔，推荐必填 |
| dislikeIngredients | string \| null | 逗号分隔忌口 |

### PUT `/user/profile`

**Body `UpdateProfileDTO`**：字段均可选，**只更新传入的字段**。

| 字段 | 约束 |
|------|------|
| nickname | max 20 |
| gender | 0~2 |
| avatarUrl | max 512 |
| school / major | max 128 |
| tasteTags / dislikeIngredients | max 256 |
| bio | max 512 |
| privacyFeed / privacyAddFriend | 0 或 1 |

若传了 `tasteTags` 且去空白后为空 → 400「口味偏好不能为空」。

**响应**：更新后的 `UserProfileVO`。

---

## 美食（用户端）

### GET `/food/categories`

无需 Token。

**响应**：`FoodCategoryVO[]` 树形结构，子节点在 `children` 数组。

| 字段 | 类型 |
|------|------|
| id | number |
| parentId | number |
| name | string |
| sortNo | number |
| children | FoodCategoryVO[] |

---

### GET `/food/list`

无需 Token。

**Query**

| 参数 | 必填 | 默认 | 说明 |
|------|------|------|------|
| categoryId | 否 | | 分类筛选 |
| campusArea | 否 | | 校区/区域 |
| page | 否 | 1 | |
| size | 否 | 10 | 最大 50 |

**响应**：`PageVO<FoodItemVO>`，仅 **上架** 美食。

---

### GET `/food/search`

无需 Token。

**Query**

| 参数 | 必填 | 说明 |
|------|------|------|
| keyword | 是 | 空白会 400 |
| page / size | 否 | 同 list |

**响应**：`PageVO<FoodItemVO>`。

---

### GET `/food/{id}`

无需 Token。路径 `id` 为美食 ID。

**响应**：`FoodItemVO`；不存在或未上架 → 400。

---

### GET `/food/recommend`

**需 Token**。

**Query**

| 参数 | 默认 | 最大 |
|------|------|------|
| limit | 10 | 50 |

**响应**：`FoodRecommendVO[]`

| 字段 | 说明 |
|------|------|
| score | number，约 0~1，四位小数 |
| recommendTags | string，命中口味标签，逗号分隔 |
| food | FoodItemVO（无 tasteTags、ingredients） |

常见 400：未完善口味、用户不存在。

---

### GET `/food/{id}/reviews`

无需 Token。

**Query**：`page`、`size`（默认 1/10，size 最大 50）。

**响应**：`PageVO<FoodReviewVO>`。

---

### POST `/food/{id}/reviews`

**需 Token**。路径 `id` 为美食 ID。

**Body `CreateReviewDTO`**

| 字段 | 类型 | 必填 |
|------|------|------|
| score | number | 是，1~5 |
| content | string | 否，最长 2000 |

**响应**：`FoodReviewVO`（新建评价）。

---

### `FoodItemVO` 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | |
| merchantId | number | |
| shopName | string | |
| campusArea | string | |
| categoryId | number | |
| name | string | |
| foodType | string | |
| price | number | 元 |
| stock | number | |
| description | string | |
| coverUrl | string | |
| avgScore | number | |
| reviewCount | number | |
| salesCount | number | |
| recommendTag | string | 招牌/新品等 |
| status | number | 1 上架 |
| tasteTags | string? | 列表接口可能不返回 |
| ingredients | string? | 列表接口可能不返回 |

---

### `FoodReviewVO` 字段

| 字段 | 类型 |
|------|------|
| id | number |
| userId | number |
| nickname | string |
| foodId | number |
| score | number |
| content | string |
| likeCount | number |
| createdAt | string | ISO 日期时间 |

---

## 商家店铺（需 Token）

### POST `/merchant/insert`

入驻申请，店主自动绑定为当前用户，默认 **待审**。

**Body**：`Merchant` 实体 JSON，常用字段：

| 字段 | 说明 |
|------|------|
| shopName | 店名 |
| campusArea | 校区 |
| address | 地址 |
| contactPhone | 电话 |
| logoUrl | Logo |
| description | 简介 |
| licenseImgUrl | 证照图 |

不必传 `ownerUserId`、`auditStatus`。

**响应**：`number`（影响行数，成功一般为 1）。

---

### PUT `/merchant/update`

**Body** 必须含 `id`，仅非 null 字段更新。

**响应**：`number` 影响行数。非本人店铺 → 400。

---

### PUT `/merchant/delete/{id}`

逻辑删除，路径为店铺 id。

**响应**：`number`。

---

### GET `/merchant/mine`

当前用户名下所有未删除店铺（含待审）。

**响应**：`Merchant[]`。

---

### GET `/merchant/list`

**仅** 审核通过且营业中的店铺（用户端列表）。

**响应**：`Merchant[]`。

---

### GET `/merchant/get/{id}`

**响应**：`Merchant` 或 `null`（JSON null）。

---

### POST `/merchant/listByCondition`

**Body `MerchantQueryDTO`**

| 字段 | 说明 |
|------|------|
| shopName | 模糊 |
| shopAddress | 模糊匹配 address |
| auditStatus / status | 精确 |
| pageNum / pageSize | 同时有值才分页 |

**响应**：`Merchant[]`。

---

### `Merchant` 主要字段

| 字段 | 说明 |
|------|------|
| id | number |
| ownerUserId | number |
| shopName | string |
| campusArea | string |
| address | string |
| contactPhone | string |
| logoUrl | string |
| description | string |
| licenseImgUrl | string |
| auditStatus | 0~3 |
| auditRemark | string |
| status | 0~2 |
| settledAt / createdAt / updatedAt | string |

---

## 店主菜品（需 Token）

店铺须 **审核通过** 后才能新增菜品；改删须为本人店铺下的菜。

### POST `/merchant/foods/save`

**Body `MerchantFoodSaveDTO`**

| 字段 | 说明 |
|------|------|
| name | 名称 |
| price | 元 |
| stock | |
| description | |
| coverUrl | |
| categoryId | |
| tasteTags | 逗号分隔 |
| ingredients | 逗号分隔 |

新增后默认 **上架**（status=1）。

**响应**：`number` 新菜品 **id**。

---

### PUT `/merchant/foods/update/{id}`

**Body**：同 save（路径 id 为准）。

**响应**：无 body（void）。

---

### PUT `/merchant/foods/delete/{id}`

逻辑删除菜品。

**响应**：无 body。

---

## 管理员（需 Token + roleId=3）

类上 `@AdminOnly`，非管理员 400「无管理员权限」。

### GET `/admin/merchants/pending`

**响应**：`Merchant[]` 待审列表。

---

### POST `/admin/merchants`

代建店铺，**直接审核通过并开业**，并给店主升商家角色。

**Body `MerchantCreateDTO`**（校验较严，见后端 DTO 注解）：

- ownerUserId（必填）
- shopName、campusArea、address、contactPhone（必填）
- logoUrl、description、licenseImgUrl（必填）

**响应**：`Merchant`。

---

### PUT `/admin/merchants/{id}/approve`

**Query**：`auditRemark` 可选。

**响应**：`Merchant`。

---

### PUT `/admin/merchants/{id}/reject`

**Body**

```json
{ "reason": "驳回原因，必填，最长 200 字" }
```

**响应**：`Merchant`。

---

## 导入 Apifox

使用 [openapi.yaml](./openapi.yaml)：**导入 → OpenAPI 3.0**，环境变量配置 `baseUrl`、`token`，登录接口后置脚本写入 token（见 [../backend/apifox-merchant.md](../backend/apifox-merchant.md) 脚本示例）。
