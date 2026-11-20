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
  code: string;
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

export type CodeTargetType = "PHONE" | "EMAIL";
export type CodeScene = "REGISTER" | "RESET";

export interface SendCodeDTO {
  target: string;
  targetType: CodeTargetType;
  scene: CodeScene;
}

export interface SendCodeVO {
  expireIn: number;
  resendAfter: number;
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

export interface UploadVO {
  /** 站内相对地址，前端拼 baseUrl 使用 */
  url: string;
  /** 文件字节数 */
  size: number;
}

export interface UserPublicVO {
  id: number;
  nickname: string;
  avatarUrl: string | null;
  bio: string | null;
  school: string | null;
  major: string | null;
  /** 动态是否公开：1 是，0 否 */
  privacyFeed: number;
}

export interface NoticeDTO {
  /** 最长 128 字 */
  title: string;
  content: string;
  /** 0 草稿（默认），1 发布 */
  publishStatus?: 0 | 1;
}

export interface NoticeVO {
  id: number;
  title: string;
  content: string;
  publishStatus: 0 | 1;
  /** 草稿为 null */
  publishedAt: string | null;
  createdAt: string;
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
  /** 最多 9 张，地址来自 POST /user/upload */
  images?: string[];
}

export interface CreateReplyDTO {
  /** 1–1000 字 */
  content: string;
}

export interface FoodReviewVO {
  id: number;
  userId: number;
  nickname: string;
  foodId: number;
  score: number;
  content: string | null;
  likeCount: number;
  /** 无图时为空数组 */
  images: string[];
  createdAt: string;
  /** 商家回复内容；3.7 / 3.8 / 6.13 为 null */
  merchantReply?: string | null;
  /** 商家回复时间；3.7 / 3.8 / 6.13 为 null */
  merchantReplyAt?: string | null;
}

export interface ReviewLikeVO {
  reviewId: number;
  liked: boolean;
  likeCount: number;
}

export interface ReviewReplyVO {
  id: number;
  reviewId: number;
  userId: number;
  nickname: string | null;
  content: string;
  /** 0 用户 / 1 商家 */
  fromMerchant: number;
  createdAt: string;
}

/* ---------- 社交 ---------- */

export interface FriendFeedVO extends FeedVO {
  userId: number;
  nickname: string;
  avatarUrl: string | null;
}

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
  /** 私聊对方用户 ID */
  peerUserId?: number;
  /** 1 私聊 / 2 群聊 */
  sessionType: 1 | 2;
  /** 群名称，仅群聊；最长 128 字 */
  title?: string;
  /** 群成员，不含自己；仅群聊，至少 1 人 */
  memberIds?: number[];
}

export interface ChatSessionVO {
  sessionId: number;
  /** 1 私聊 / 2 群聊 */
  sessionType: 1 | 2;
  /** 群名称，私聊为 null */
  title: string | null;
  /** 私聊固定 2，群聊含自己 */
  memberCount: number;
  /** 群聊为 null */
  peerUserId: number | null;
  /** 群聊为 null */
  peerNickname: string | null;
  /** 群聊为 null */
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

export interface UserStatusUpdateDTO {
  /** 0 封禁，1 恢复 */
  status: 0 | 1;
  /** 封禁原因，封禁时必填 */
  reason?: string | null;
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
