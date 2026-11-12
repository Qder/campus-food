/**
 * 拷进前端工程即可。字段对齐 docs/校园美食平台完整接口文档.md（已实现部分）。
 * 成功：HTTP 200，body 就是这些类型，没有 { code, data } 包装。
 * 与文档不一致时以文档为准。还没做的接口不要按这里先调。
 */

export interface PageVO<T> {
  total: number;
  page: number;
  size: number;
  list: T[];
}

export interface ApiErrorBody {
  code: number;
  message: string;
}

export type RoleId = 1 | 2 | 3;

/* ---------- 认证 ---------- */

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

/* ---------- 用户 ---------- */

export interface UserProfileVO {
  id: number;
  phone: string | null;
  email: string | null;
  nickname: string;
  avatarUrl: string | null;
  gender: number;
  school: string | null;
  major: string | null;
  bio: string | null;
  privacyFeed: number;
  privacyAddFriend: number;
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

export interface AddFavoritiesDTO {
  targetId: number;
  targetType: 1 | 2;
}

export interface DeleteFavoritiesDTO {
  targetId: number;
  targetType: 1 | 2;
}

export interface FavoritiesVO {
  favoriteId: number;
  targetType: 1 | 2;
  targetId: number;
  title: string;
  subTitle: string | null;
  coverUrl: string | null;
  price: number | null;
  avgScore: number | null;
  status: number | null;
  createdAt: string;
}

export interface FavoriteAddVO {
  targetType: 1 | 2;
  targetId: number;
  favorited: boolean;
}

export interface ReminderDTO {
  remindType: 'CAMPAIGN' | 'FRIEND_FEED' | 'ORDER';
  enabled: boolean;
  channel?: string;
}

export interface RemindVO {
  remindType: string;
  label: string;
  channel: string;
  enabled: boolean;
}

export interface AddFeedDTO {
  content: string;
  images?: string[];
  privacy?: number;
}

export interface FeedVO {
  feedId: number;
  content: string;
  images: string[];
  privacy: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
}

export interface FeedPublishVO {
  feedId: number;
  auditStatus: number;
}

/* ---------- 美食 ---------- */

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
  campusArea: string | null;
  categoryId: number | null;
  name: string;
  foodType: string | null;
  price: number;
  stock: number;
  description: string | null;
  coverUrl: string | null;
  avgScore: number | null;
  reviewCount: number;
  salesCount: number;
  recommendTag: string | null;
  status: number;
  tasteTags?: string;
  ingredients?: string;
}

export interface FoodRecommendVO {
  recommendTags: string | null;
  score: number;
  food: FoodItemVO;
}

export interface CreateReviewDTO {
  score: number;
  content?: string;
}

export interface FoodReviewVO {
  id: number;
  userId: number;
  nickname: string;
  foodId: number;
  score: number;
  content: string | null;
  likeCount: number;
  createdAt: string;
}

/* ---------- 社交 ---------- */

export interface FriendRequestVo {
  id: number;
  fromUserId: number;
  fromNickname: string | null;
  toUserId: number;
  message: string | null;
  status: number;
  createdAt: string;
  handledAt: string | null;
}

export interface FriendVo {
  id: number;
  nickname: string;
  avatarUrl: string | null;
}

export interface CreateSessionDTO {
  peerUserId: number;
  sessionType: 1 | 2;
}

export interface ChatSessionVO {
  sessionId: number;
  sessionType: number;
  peerUserId: number;
  peerNickname: string;
  peerAvatarUrl: string | null;
}

export interface SendMessageDTO {
  msgType: 'TEXT' | 'IMAGE' | 'EMOJI';
  content: string;
  clientMsgId?: string;
}

export interface ChatMessageVO {
  msgId: string;
  clientMsgId: string | null;
  sessionId: number;
  fromUserId: number;
  fromNickname: string;
  msgType: string;
  content: string;
  createdAt: string;
}

export interface ChatMessagePageVO {
  sessionId: number;
  hasMore: boolean;
  list: ChatMessageVO[];
}

export interface BlockUserDTO {
  userId: number;
}

export interface BlockResultVO {
  userId: number;
  blocked: boolean;
}

export interface BlacklistItemVO {
  userId: number;
  nickname: string;
  avatarUrl: string | null;
  blockedAt: string;
}

/* ---------- 拼单 ---------- */

export interface CreateMealActivityDTO {
  title: string;
  meetTime: string;
  meetPlace: string;
  foodType?: string;
  relatedFoodId?: number;
  relatedMerchantId?: number;
  maxMembers: number;
  description?: string;
  signupDeadline?: string;
}

export interface MealActivityMemberVO {
  userId: number;
  nickname: string;
  avatarUrl: string | null;
  role: number;
  status: number;
  joinedAt?: string;
}

export interface MealActivityVO {
  activityId: number;
  title: string;
  creatorId: number;
  creatorNickname: string;
  meetTime: string;
  meetPlace: string;
  foodType: string | null;
  relatedFoodId: number | null;
  relatedFoodName: string | null;
  relatedMerchantId: number | null;
  maxMembers: number;
  currentMembers: number;
  description: string | null;
  signupDeadline: string | null;
  status: number;
  joined: boolean;
  isCreator: boolean;
  members?: MealActivityMemberVO[];
}

export interface MealActivityCreateVO {
  activityId: number;
  status: number;
  currentMembers: number;
  createdAt: string;
}

export interface MealJoinResultVO {
  activityId: number;
  joined: boolean;
  currentMembers: number;
  status?: number;
}

export interface MealRemoveResultVO {
  activityId: number;
  userId: number;
  currentMembers: number;
}

export interface CancelMealActivityDTO {
  reason?: string;
}

export interface MealCancelResultVO {
  activityId: number;
  status: number;
}

export interface SubmitMealFeedbackDTO {
  toUserId?: number;
  score: number;
  content?: string;
}

export interface MealFeedbackCreateVO {
  feedbackId: number;
  activityId: number;
}

export interface MealFeedbackVO {
  feedbackId: number;
  fromUserId: number;
  fromNickname: string;
  toUserId: number | null;
  score: number;
  content: string | null;
  createdAt: string;
}

/* ---------- 商家 ---------- */

export interface Merchant {
  id: number;
  ownerUserId: number;
  shopName: string;
  campusArea: string | null;
  address: string | null;
  contactPhone: string | null;
  logoUrl: string | null;
  description: string | null;
  licenseImgUrl: string | null;
  auditStatus: number;
  auditRemark: string | null;
  auditedAt: string | null;
  auditedBy: number | null;
  status: number;
  settledAt: string | null;
  createdAt: string | null;
  updatedAt: string | null;
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
  stock: number;
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

export interface UserListVO {
  id: number;
  nickname: string;
  phone: string | null;
  email: string | null;
  roleId: RoleId;
  status: 0 | 1;
}

/* ---------- 订单 ---------- */

export interface OrderLineDTO {
  foodId: number;
  quantity: number;
}

export interface PlaceOrderDTO {
  items: OrderLineDTO[];
  deliveryType: 1 | 2;
  deliveryAddress?: string;
  remark?: string;
  payChannel?: 'MOCK' | 'WECHAT' | 'ALIPAY';
  clientOrderToken: string;
}

export interface OrderPlaceVO {
  orderId: number;
  orderNo: string;
  status: string;
  payAmount: number;
}

export interface OrderSummaryVO {
  orderId: number;
  orderNo: string;
  merchantId: number;
  shopName: string;
  payAmount: number;
  deliveryType: number;
  status: string;
  createdAt: string;
}

export interface OrderItemLineVO {
  foodId: number;
  foodName: string;
  unitPrice: number;
  quantity: number;
  amount: number;
}

export interface OrderDetailVO {
  orderId: number;
  orderNo: string;
  merchantId: number;
  shopName: string;
  items: OrderItemLineVO[];
  totalAmount: number;
  payAmount: number;
  deliveryType: number;
  deliveryAddress: string | null;
  remark: string | null;
  status: string;
  cancelReason: string | null;
  paidAt: string | null;
  acceptedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  createdAt: string;
}
