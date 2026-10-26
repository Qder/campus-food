-- =============================================================================
-- 校园美食智能推荐与社交平台 — MySQL 8 DDL
-- 字符集: utf8mb4 / utf8mb4_unicode_ci
-- 说明: 主键、索引、表/字段注释齐全；配合开发文档使用
-- =============================================================================

CREATE DATABASE IF NOT EXISTS campus_food
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_food;

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 1. 用户 / 角色 / 认证
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS sys_user_oauth;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  email           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  password_hash   VARCHAR(100) NOT NULL COMMENT '密码哈希(BCrypt等)',
  nickname        VARCHAR(64)  NOT NULL COMMENT '昵称',
  avatar_url      VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  gender          TINYINT      NOT NULL DEFAULT 0 COMMENT '性别:0未知1男2女',
  school          VARCHAR(128) DEFAULT NULL COMMENT '所在学校',
  major           VARCHAR(128) DEFAULT NULL COMMENT '专业',
  bio             VARCHAR(512) DEFAULT NULL COMMENT '个人简介',
  privacy_feed    TINYINT      NOT NULL DEFAULT 1 COMMENT '动态是否公开:0否1是',
  privacy_add_friend TINYINT   NOT NULL DEFAULT 1 COMMENT '是否允许陌生人加好友:0否1是',
  status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用1正常',
  role_id         TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '角色ID，对应sys_role.id，默认普通用户(USER)',
  last_login_at   DATETIME     DEFAULT NULL COMMENT '最近登录时间',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0否1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_phone (phone),
  UNIQUE KEY uk_user_email (email),
  KEY idx_user_school (school),
  KEY idx_user_status (status),
  KEY idx_user_created (created_at),
  KEY idx_user_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE sys_role (
  id          TINYINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  code        VARCHAR(32)  NOT NULL COMMENT '角色编码 USER/MERCHANT/ADMIN',
  name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
  remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';

CREATE TABLE sys_user_role (
  id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id  BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  role_id  TINYINT UNSIGNED NOT NULL COMMENT '角色ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联';

CREATE TABLE sys_user_oauth (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  provider      VARCHAR(32)  NOT NULL COMMENT '提供方:WECHAT/QQ',
  open_id       VARCHAR(128) NOT NULL COMMENT '第三方OpenId',
  union_id      VARCHAR(128) DEFAULT NULL COMMENT 'UnionId',
  extra_json    JSON DEFAULT NULL COMMENT '扩展信息',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_oauth_provider_openid (provider, open_id),
  KEY idx_oauth_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方账号绑定';

CREATE TABLE sys_login_log (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id     BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
  login_type  VARCHAR(32) NOT NULL COMMENT '登录方式:PASSWORD/SMS/OAUTH',
  ip          VARCHAR(64) DEFAULT NULL COMMENT 'IP',
  user_agent  VARCHAR(512) DEFAULT NULL COMMENT 'UA',
  success     TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功',
  message     VARCHAR(255) DEFAULT NULL COMMENT '说明',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (id),
  KEY idx_login_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志';

-- -----------------------------------------------------------------------------
-- 2. 商家 / 店铺
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS merchant_qualification;
DROP TABLE IF EXISTS merchant;

CREATE TABLE merchant (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商家/店铺ID',
  owner_user_id   BIGINT UNSIGNED NOT NULL COMMENT '店主用户ID',
  shop_name       VARCHAR(128) NOT NULL COMMENT '店铺名称',
  campus_area     VARCHAR(64)  DEFAULT NULL COMMENT '校园区域(食堂/商圈)',
  address         VARCHAR(255) NOT NULL COMMENT '地址',
  contact_phone   VARCHAR(20)  NOT NULL COMMENT '联系电话',
  logo_url        VARCHAR(512) DEFAULT NULL COMMENT '店铺Logo',
  description     VARCHAR(1000) DEFAULT NULL COMMENT '店铺简介',
  license_img_url VARCHAR(512) DEFAULT NULL COMMENT '营业执照图片URL',
  audit_status    TINYINT NOT NULL DEFAULT 0 COMMENT '审核:0待审1通过2驳回3取消资格',
  audit_remark    VARCHAR(512) DEFAULT NULL COMMENT '管理员审核备注',
  audited_at      DATETIME DEFAULT NULL COMMENT '审核时间',
  audited_by      BIGINT UNSIGNED DEFAULT NULL COMMENT '审核管理员ID',
  status          TINYINT NOT NULL DEFAULT 0 COMMENT '经营状态:0未开业1营业2停业',
  settled_at      DATETIME DEFAULT NULL COMMENT '入驻通过时间',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_merchant_owner (owner_user_id),
  KEY idx_merchant_area (campus_area),
  KEY idx_merchant_audit (audit_status),
  KEY idx_merchant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家店铺';

CREATE TABLE merchant_qualification (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  merchant_id  BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  doc_type     VARCHAR(64) NOT NULL COMMENT '证件类型',
  doc_url      VARCHAR(512) NOT NULL COMMENT '证件图片URL',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (id),
  KEY idx_mq_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家资质附件';

-- -----------------------------------------------------------------------------
-- 3. 美食 / 分类 / 图片
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS food_image;
DROP TABLE IF EXISTS food_item;
DROP TABLE IF EXISTS food_category;

CREATE TABLE food_category (
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  parent_id   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID,0为顶级',
  name        VARCHAR(64) NOT NULL COMMENT '分类名(中餐/西餐/小吃等)',
  sort_no     INT NOT NULL DEFAULT 0 COMMENT '排序',
  status      TINYINT NOT NULL DEFAULT 1 COMMENT '1启用0停用',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_fc_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食分类';

CREATE TABLE food_item (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '美食ID',
  merchant_id     BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  category_id     INT UNSIGNED DEFAULT NULL COMMENT '分类ID',
  name            VARCHAR(128) NOT NULL COMMENT '美食名称',
  food_type       VARCHAR(64) DEFAULT NULL COMMENT '类型标签冗余',
  price           DECIMAL(10,2) NOT NULL COMMENT '价格(元)',
  stock           INT NOT NULL DEFAULT 0 COMMENT '库存',
  description     VARCHAR(2000) DEFAULT NULL COMMENT '描述',
  cover_url       VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  avg_score       DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '平均评分',
  review_count    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评价数',
  sales_count     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
  recommend_tag   VARCHAR(128) DEFAULT NULL COMMENT '推荐标签(招牌/新品等)',
  status          TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿1上架2下架',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_food_merchant (merchant_id),
  KEY idx_food_category (category_id),
  KEY idx_food_status_score (status, avg_score),
  KEY idx_food_name (name),
  FULLTEXT KEY ft_food_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食商品';

CREATE TABLE food_image (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  food_id     BIGINT UNSIGNED NOT NULL COMMENT '美食ID',
  url         VARCHAR(512) NOT NULL COMMENT '图片URL',
  sort_no     INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_fi_food (food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食图片';

-- -----------------------------------------------------------------------------
-- 4. 评价 / 点赞 / 回复
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS review_reply;
DROP TABLE IF EXISTS review_like;
DROP TABLE IF EXISTS review_image;
DROP TABLE IF EXISTS food_review;

CREATE TABLE food_review (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  food_id       BIGINT UNSIGNED NOT NULL COMMENT '美食ID',
  merchant_id   BIGINT UNSIGNED NOT NULL COMMENT '商家ID冗余',
  order_id      BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
  score         TINYINT NOT NULL COMMENT '评分1-5',
  content       VARCHAR(2000) DEFAULT NULL COMMENT '文字评价',
  like_count    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  audit_status  TINYINT NOT NULL DEFAULT 0 COMMENT '审核:0待审1通过2拒绝',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '1展示0隐藏',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_review_food (food_id, created_at),
  KEY idx_review_user (user_id),
  KEY idx_review_merchant (merchant_id),
  KEY idx_review_audit (audit_status),
  KEY idx_review_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美食评价';

CREATE TABLE review_image (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  review_id   BIGINT UNSIGNED NOT NULL COMMENT '评价ID',
  url         VARCHAR(512) NOT NULL COMMENT '图片URL',
  sort_no     INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_ri_review (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价图片';

CREATE TABLE review_like (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  review_id   BIGINT UNSIGNED NOT NULL COMMENT '评价ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_review_like (review_id, user_id),
  KEY idx_rl_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价点赞';

CREATE TABLE review_reply (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  review_id     BIGINT UNSIGNED NOT NULL COMMENT '评价ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '回复人用户ID',
  reply_to_uid  BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复用户ID',
  content       VARCHAR(1000) NOT NULL COMMENT '回复内容',
  from_merchant TINYINT NOT NULL DEFAULT 0 COMMENT '是否商家回复:0否1是',
  audit_status  TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
  PRIMARY KEY (id),
  KEY idx_rr_review (review_id),
  KEY idx_rr_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价回复';

-- -----------------------------------------------------------------------------
-- 5. 收藏 / 关注 / 提醒
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS user_reminder;
DROP TABLE IF EXISTS user_follow_merchant;
DROP TABLE IF EXISTS user_favorite;

CREATE TABLE user_favorite (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  target_type   TINYINT NOT NULL COMMENT '类型:1美食2店铺',
  target_id     BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_fav (user_id, target_type, target_id),
  KEY idx_fav_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏(美食/店铺)';

CREATE TABLE user_follow_merchant (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id      BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  merchant_id  BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_follow (user_id, merchant_id),
  KEY idx_follow_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注店铺';

CREATE TABLE user_reminder (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  remind_type   VARCHAR(32) NOT NULL COMMENT '类型:CAMPAIGN/FRIEND_FEED/ORDER等',
  channel       VARCHAR(32) NOT NULL DEFAULT 'INBOX' COMMENT '渠道:INBOX/SMS/PUSH',
  enabled       TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启',
  ref_type      VARCHAR(32) DEFAULT NULL COMMENT '关联类型',
  ref_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_reminder_user (user_id, remind_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户提醒设置';

-- -----------------------------------------------------------------------------
-- 6. 好友 / 私聊会话元数据 / 群聊
-- 聊天明细建议存 MongoDB；此处保留会话与群组关系及可选消息索引表
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS chat_message_index;
DROP TABLE IF EXISTS chat_session_member;
DROP TABLE IF EXISTS chat_group_member;
DROP TABLE IF EXISTS chat_group;
DROP TABLE IF EXISTS chat_session;
DROP TABLE IF EXISTS user_blacklist;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS friend_request;

CREATE TABLE friend_request (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  from_user_id  BIGINT UNSIGNED NOT NULL COMMENT '申请人',
  to_user_id    BIGINT UNSIGNED NOT NULL COMMENT '被申请人',
  message       VARCHAR(255) DEFAULT NULL COMMENT '申请留言',
  status        TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理1同意2拒绝3过期',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  handled_at    DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (id),
  KEY idx_fr_to_status (to_user_id, status),
  KEY idx_fr_from (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请';

CREATE TABLE friendship (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  friend_id     BIGINT UNSIGNED NOT NULL COMMENT '好友用户ID',
  remark        VARCHAR(64) DEFAULT NULL COMMENT '备注名',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_friendship (user_id, friend_id),
  KEY idx_friendship_friend (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系(双向各一行)';

CREATE TABLE user_blacklist (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  blocked_uid   BIGINT UNSIGNED NOT NULL COMMENT '被拉黑用户ID',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_blacklist (user_id, blocked_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户黑名单';

CREATE TABLE chat_session (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  session_type    TINYINT NOT NULL COMMENT '1私聊2群聊',
  peer_user_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '私聊对方用户ID(冗余便于查)',
  group_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '群ID',
  last_msg_preview VARCHAR(255) DEFAULT NULL COMMENT '最后消息摘要',
  last_msg_at     DATETIME DEFAULT NULL COMMENT '最后消息时间',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_cs_group (group_id),
  KEY idx_cs_last (last_msg_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话';

CREATE TABLE chat_session_member (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  session_id  BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '成员用户ID',
  unread_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '未读数',
  joined_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_csm (session_id, user_id),
  KEY idx_csm_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员';

CREATE TABLE chat_group (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '群ID',
  name          VARCHAR(128) NOT NULL COMMENT '群名称',
  owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '群主用户ID',
  avatar_url    VARCHAR(512) DEFAULT NULL COMMENT '群头像',
  notice        VARCHAR(500) DEFAULT NULL COMMENT '群公告',
  max_members   INT NOT NULL DEFAULT 200 COMMENT '人数上限',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '1正常0解散',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_cg_owner (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊';

CREATE TABLE chat_group_member (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  group_id    BIGINT UNSIGNED NOT NULL COMMENT '群ID',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  role        TINYINT NOT NULL DEFAULT 0 COMMENT '0成员1管理员2群主',
  nickname_in_group VARCHAR(64) DEFAULT NULL COMMENT '群内昵称',
  joined_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cgm (group_id, user_id),
  KEY idx_cgm_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员';

CREATE TABLE chat_message_index (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息索引ID',
  mongo_msg_id  VARCHAR(64) NOT NULL COMMENT 'Mongo消息ID',
  session_id    BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  from_user_id  BIGINT UNSIGNED NOT NULL COMMENT '发送者',
  msg_type      VARCHAR(16) NOT NULL COMMENT 'TEXT/IMAGE/EMOJI',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mongo_msg (mongo_msg_id),
  KEY idx_cmi_session_time (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息索引(正文在MongoDB)';

-- -----------------------------------------------------------------------------
-- 7. 拼单约饭
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS meal_activity_feedback;
DROP TABLE IF EXISTS meal_activity_member;
DROP TABLE IF EXISTS meal_activity;

CREATE TABLE meal_activity (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  creator_id      BIGINT UNSIGNED NOT NULL COMMENT '发起者用户ID',
  title           VARCHAR(128) NOT NULL COMMENT '活动名称',
  meet_time       DATETIME NOT NULL COMMENT '活动时间',
  meet_place      VARCHAR(255) NOT NULL COMMENT '地点',
  food_type       VARCHAR(64) DEFAULT NULL COMMENT '美食类型偏好',
  related_food_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联美食ID',
  related_merchant_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联店铺ID',
  max_members     INT NOT NULL DEFAULT 10 COMMENT '人数上限',
  current_members INT NOT NULL DEFAULT 1 COMMENT '当前人数(含发起人)',
  description     VARCHAR(1000) DEFAULT NULL COMMENT '说明',
  signup_deadline DATETIME DEFAULT NULL COMMENT '报名截止',
  status          TINYINT NOT NULL DEFAULT 0 COMMENT '0报名中1已满2进行中3结束4取消',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_ma_creator (creator_id),
  KEY idx_ma_time_status (meet_time, status),
  KEY idx_ma_food_type (food_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼单约饭活动';

CREATE TABLE meal_activity_member (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  activity_id   BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '报名用户ID',
  role          TINYINT NOT NULL DEFAULT 0 COMMENT '0成员1发起人',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '1已报名2退出3被移除',
  joined_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mam (activity_id, user_id),
  KEY idx_mam_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼单约饭报名';

CREATE TABLE meal_activity_feedback (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  activity_id   BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  from_user_id  BIGINT UNSIGNED NOT NULL COMMENT '评价人',
  to_user_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '被评价人(可空表示对活动)',
  score         TINYINT DEFAULT NULL COMMENT '评分1-5',
  content       VARCHAR(500) DEFAULT NULL COMMENT '反馈内容',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (id),
  KEY idx_maf_activity (activity_id),
  KEY idx_maf_from (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼单约饭结束后反馈';

-- -----------------------------------------------------------------------------
-- 8. 订单 / 支付
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS payment_record;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS food_order;

CREATE TABLE food_order (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  order_no        VARCHAR(64) NOT NULL COMMENT '业务订单号',
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  merchant_id     BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  total_amount    DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  pay_amount      DECIMAL(12,2) NOT NULL COMMENT '实付金额',
  campaign_id     BIGINT UNSIGNED DEFAULT NULL COMMENT '使用的活动ID',
  status          VARCHAR(32) NOT NULL COMMENT 'PENDING_PAY/PAID/ACCEPTED/COMPLETED/CANCELLED',
  delivery_type   TINYINT NOT NULL DEFAULT 1 COMMENT '1自提2配送',
  delivery_address VARCHAR(255) DEFAULT NULL COMMENT '配送地址',
  remark          VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  paid_at         DATETIME DEFAULT NULL COMMENT '支付时间',
  accepted_at     DATETIME DEFAULT NULL COMMENT '接单时间',
  completed_at    DATETIME DEFAULT NULL COMMENT '完成时间',
  cancelled_at    DATETIME DEFAULT NULL COMMENT '取消时间',
  cancel_reason   VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user_time (user_id, created_at),
  KEY idx_order_merchant_status (merchant_id, status),
  KEY idx_order_status_time (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE order_item (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  order_id      BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  food_id       BIGINT UNSIGNED NOT NULL COMMENT '美食ID',
  food_name     VARCHAR(128) NOT NULL COMMENT '下单时菜名快照',
  unit_price    DECIMAL(10,2) NOT NULL COMMENT '单价快照',
  quantity      INT NOT NULL DEFAULT 1 COMMENT '数量',
  amount        DECIMAL(12,2) NOT NULL COMMENT '小计',
  PRIMARY KEY (id),
  KEY idx_oi_order (order_id),
  KEY idx_oi_food (food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细';

CREATE TABLE payment_record (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  order_id        BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  order_no        VARCHAR(64) NOT NULL COMMENT '订单号',
  pay_channel     VARCHAR(32) NOT NULL COMMENT '支付渠道:MOCK/WECHAT/ALIPAY',
  trade_no        VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
  amount          DECIMAL(12,2) NOT NULL COMMENT '支付金额',
  status          TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付1成功2失败3退款',
  callback_raw    TEXT DEFAULT NULL COMMENT '回调原文',
  paid_at         DATETIME DEFAULT NULL COMMENT '成功时间',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_pay_order (order_id),
  UNIQUE KEY uk_pay_trade (pay_channel, trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录';

-- -----------------------------------------------------------------------------
-- 9. 营销活动
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS campaign_food;
DROP TABLE IF EXISTS campaign_stat;
DROP TABLE IF EXISTS marketing_campaign;

CREATE TABLE marketing_campaign (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  merchant_id     BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  title           VARCHAR(128) NOT NULL COMMENT '活动标题',
  campaign_type   VARCHAR(32) NOT NULL COMMENT 'DISCOUNT/FULL_REDUCE/GROUP_BUY',
  rule_json       JSON NOT NULL COMMENT '规则JSON(折扣/满减门槛等)',
  start_at        DATETIME NOT NULL COMMENT '开始时间',
  end_at          DATETIME NOT NULL COMMENT '结束时间',
  status          TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿1进行中2结束3下线',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_mc_merchant (merchant_id),
  KEY idx_mc_time (start_at, end_at),
  KEY idx_mc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动';

CREATE TABLE campaign_food (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  campaign_id  BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  food_id      BIGINT UNSIGNED NOT NULL COMMENT '适用美食ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cf (campaign_id, food_id),
  KEY idx_cf_food (food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动适用菜品';

CREATE TABLE campaign_stat (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  campaign_id     BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  participant_cnt INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '参与人数',
  order_cnt       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单数',
  sales_amount    DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '销售额',
  stat_date       DATE NOT NULL COMMENT '统计日',
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cs (campaign_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动效果按日统计';

-- -----------------------------------------------------------------------------
-- 10. 审核 / 公告 / 处罚 / 内容
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS platform_notice;
DROP TABLE IF EXISTS recommend_slot;
DROP TABLE IF EXISTS user_punishment;
DROP TABLE IF EXISTS content_audit;

CREATE TABLE content_audit (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核单ID',
  content_type  VARCHAR(32) NOT NULL COMMENT 'REVIEW/REPLY/FEED/MERCHANT_INFO/IMAGE',
  content_id    BIGINT UNSIGNED NOT NULL COMMENT '业务内容ID',
  submit_uid    BIGINT UNSIGNED DEFAULT NULL COMMENT '提交用户',
  title         VARCHAR(255) DEFAULT NULL COMMENT '摘要标题',
  snapshot      TEXT DEFAULT NULL COMMENT '内容快照',
  status        TINYINT NOT NULL DEFAULT 0 COMMENT '0待审1通过2拒绝',
  auditor_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人',
  audit_remark  VARCHAR(512) DEFAULT NULL COMMENT '审核意见',
  audited_at    DATETIME DEFAULT NULL COMMENT '审核时间',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (id),
  KEY idx_ca_status_time (status, created_at),
  KEY idx_ca_type_content (content_type, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容/资质审核工单';

CREATE TABLE user_punishment (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  action        VARCHAR(32) NOT NULL COMMENT 'WARN/LIMIT/BAN',
  reason        VARCHAR(512) NOT NULL COMMENT '原因',
  operator_id   BIGINT UNSIGNED NOT NULL COMMENT '操作管理员',
  expire_at     DATETIME DEFAULT NULL COMMENT '处罚到期(空为永久)',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处罚时间',
  PRIMARY KEY (id),
  KEY idx_up_user (user_id),
  KEY idx_up_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户处罚记录';

CREATE TABLE platform_notice (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title         VARCHAR(128) NOT NULL COMMENT '标题',
  content       TEXT NOT NULL COMMENT '正文',
  publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿1已发布',
  publisher_id  BIGINT UNSIGNED NOT NULL COMMENT '发布人',
  published_at  DATETIME DEFAULT NULL COMMENT '发布时间',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_pn_status_time (publish_status, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台公告';

CREATE TABLE recommend_slot (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '推荐位ID',
  slot_code     VARCHAR(64) NOT NULL COMMENT '位置编码(HOME_BANNER等)',
  title         VARCHAR(128) DEFAULT NULL COMMENT '标题',
  target_type   VARCHAR(32) NOT NULL COMMENT 'FOOD/MERCHANT/URL/CAMPAIGN',
  target_id     BIGINT UNSIGNED DEFAULT NULL COMMENT '目标ID',
  link_url      VARCHAR(512) DEFAULT NULL COMMENT '外链',
  image_url     VARCHAR(512) DEFAULT NULL COMMENT '展示图',
  sort_no       INT NOT NULL DEFAULT 0 COMMENT '排序',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '1启用0停用',
  start_at      DATETIME DEFAULT NULL COMMENT '生效开始',
  end_at        DATETIME DEFAULT NULL COMMENT '生效结束',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_rs_code_status (slot_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页/活动推荐位';

-- -----------------------------------------------------------------------------
-- 11. 站内消息 / 行为 / 推荐结果 / 统计
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS stat_merchant_daily;
DROP TABLE IF EXISTS stat_daily;
DROP TABLE IF EXISTS rec_user_item;
DROP TABLE IF EXISTS user_behavior;
DROP TABLE IF EXISTS inbox_message;

CREATE TABLE inbox_message (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '接收用户',
  msg_type      VARCHAR(32) NOT NULL COMMENT 'SYSTEM/CAMPAIGN/FRIEND/ORDER',
  title         VARCHAR(128) NOT NULL COMMENT '标题',
  content       VARCHAR(1000) NOT NULL COMMENT '内容',
  ref_type      VARCHAR(32) DEFAULT NULL COMMENT '关联类型',
  ref_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID',
  is_read       TINYINT NOT NULL DEFAULT 0 COMMENT '0未读1已读',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (id),
  KEY idx_inbox_user_read (user_id, is_read, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信';

CREATE TABLE user_behavior (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  behavior_type VARCHAR(32) NOT NULL COMMENT 'VIEW/CLICK/FAV/ORDER/SCORE',
  food_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '美食ID',
  merchant_id   BIGINT UNSIGNED DEFAULT NULL COMMENT '商家ID',
  score         DECIMAL(5,2) DEFAULT NULL COMMENT '隐式/显式权重',
  extra_json    JSON DEFAULT NULL COMMENT '扩展',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (id),
  KEY idx_ub_user_time (user_id, created_at),
  KEY idx_ub_food_type (food_id, behavior_type),
  KEY idx_ub_type_time (behavior_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为(供推荐)';

CREATE TABLE rec_user_item (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  food_id       BIGINT UNSIGNED NOT NULL COMMENT '推荐美食ID',
  score         DECIMAL(10,6) NOT NULL COMMENT '推荐分',
  algo          VARCHAR(32) NOT NULL DEFAULT 'ITEM_CF' COMMENT '算法标识',
  batch_no      VARCHAR(64) NOT NULL COMMENT '批次号',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_rec (user_id, food_id, batch_no),
  KEY idx_rec_user_score (user_id, score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个性化推荐结果';

CREATE TABLE stat_daily (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  stat_date       DATE NOT NULL COMMENT '统计日',
  user_total      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计用户',
  user_new        INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '新增用户',
  dau             INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '日活',
  order_cnt       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单数',
  gmv             DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '成交额',
  review_cnt      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评价数',
  merchant_active INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '活跃商家数',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '写入时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_stat_daily (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台日统计';

CREATE TABLE stat_merchant_daily (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  stat_date       DATE NOT NULL COMMENT '统计日',
  merchant_id     BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
  order_cnt       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单数',
  sales_amount    DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '销售额',
  avg_score       DECIMAL(3,2) DEFAULT NULL COMMENT '当日均分',
  review_cnt      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评价数',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '写入时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_smd (stat_date, merchant_id),
  KEY idx_smd_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家日经营统计';

-- -----------------------------------------------------------------------------
-- 12. 初始化角色
-- -----------------------------------------------------------------------------

INSERT INTO sys_role (id, code, name, remark) VALUES
  (1, 'USER', '普通用户', '默认注册角色'),
  (2, 'MERCHANT', '商家用户', '入驻审核通过后授予'),
  (3, 'ADMIN', '平台管理员', '后台初始化账号')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- =============================================================================
-- END
-- =============================================================================
