/**
 * 与 campus-food 后端 VO/DTO 对齐的类型定义。
 * 复制到前端项目的 types/campus-food.ts 使用即可。
 */

/** 业务错误体（400/500）；401 拦截器同样使用该结构 */
export interface ApiErrorBody {
  code: number;
  message: string;
}

export interface RegisterDTO {
  phone?: string;
  email?: string;
  password: string;
  nickname: string;
}

export interface RegisterVO {
  id: number;
  nickname: string;
  roleId: number;
}

export interface LoginDTO {
  username: string;
  password: string;
}

export interface LoginVO {
  id: number;
  nickname: string;
  roleId: number;
  token: string;
}

export interface UserProfileVO {
  id: number;
  phone: string | null;
  email: string | null;
  nickname: string;
  avatarUrl: string | null;
  gender: number | null;
  school: string | null;
  major: string | null;
  bio: string | null;
  privacyFeed: number | null;
  privacyAddFriend: number | null;
  roleId: number;
  status: number;
  tasteTags: string | null;
  dislikeIngredients: string | null;
}

export interface UpdateProfileDTO {
  nickname?: string;
  gender?: number;
  avatarUrl?: string;
  school?: string;
  major?: string;
  tasteTags?: string;
  dislikeIngredients?: string;
  bio?: string;
  privacyFeed?: number;
  privacyAddFriend?: number;
}

export interface PageVO<T> {
  total: number;
  page: number;
  size: number;
  list: T[];
}

export interface FoodCategoryVO {
  id: number;
  parentId: number;
  name: string;
  sortNo: number;
  children: FoodCategoryVO[];
}

export interface FoodItemVO {
  id: number;
  merchantId: number;
  shopName: string;
  campusArea: string;
  categoryId: number;
  name: string;
  foodType: string;
  price: number;
  stock: number;
  description: string;
  coverUrl: string;
  avgScore: number;
  reviewCount: number;
  salesCount: number;
  recommendTag: string;
  status: number;
  tasteTags?: string;
  ingredients?: string;
}

export interface FoodRecommendVO {
  score: number;
  recommendTags: string;
  food: FoodItemVO;
}

export interface FoodReviewVO {
  id: number;
  userId: number;
  nickname: string;
  foodId: number;
  score: number;
  content: string;
  likeCount: number;
  createdAt: string;
}

export interface CreateReviewDTO {
  score: number;
  content?: string;
}

export interface Merchant {
  id: number;
  ownerUserId: number;
  shopName: string;
  campusArea: string;
  address: string;
  contactPhone: string;
  logoUrl?: string;
  description?: string;
  licenseImgUrl?: string;
  auditStatus: number;
  auditRemark?: string;
  auditedAt?: string;
  auditedBy?: number;
  status: number;
  settledAt?: string;
  createdAt?: string;
  updatedAt?: string;
  deleted?: number;
}

export interface MerchantQueryDTO {
  shopName?: string;
  shopAddress?: string;
  auditStatus?: number;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

export interface MerchantFoodSaveDTO {
  id?: number;
  name: string;
  price: number;
  stock?: number;
  description?: string;
  coverUrl?: string;
  categoryId?: number;
  tasteTags?: string;
  ingredients?: string;
}

export interface MerchantCreateDTO {
  ownerUserId: number;
  shopName: string;
  campusArea: string;
  address: string;
  contactPhone: string;
  logoUrl: string;
  description: string;
  licenseImgUrl: string;
}

export interface MerchantRejectDTO {
  reason: string;
}

/** 角色 */
export const RoleId = {
  USER: 1,
  MERCHANT: 2,
  ADMIN: 3,
} as const;

/** 店铺审核 */
export const MerchantAuditStatus = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
  DISQUALIFIED: 3,
} as const;
