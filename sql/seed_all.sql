-- =============================================================================
-- 校园美食平台 · 演示数据整库重置脚本
-- 适用：campus_food（MySQL 8）
-- 说明：
--   1) 不 DROP TABLE、不 ALTER TABLE，只清空业务表并重新插入；
--   2) 保留 sys_role 角色种子；
--   3) 时间全部写死，范围 2026-11-01 ~ 2026-11-18；
--   4) 所有账号密码统一 123456；
--   5) 执行前请先 mysqldump 备份。
-- =============================================================================

SET NAMES utf8mb4;

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 0. 清空业务表（子表 -> 主表；sys_role 不动）
-- -----------------------------------------------------------------------------

DELETE FROM review_reply;
DELETE FROM review_like;
DELETE FROM review_image;
DELETE FROM food_review;

DELETE FROM food_image;
DELETE FROM food_item;
DELETE FROM food_category;

DELETE FROM merchant_qualification;
DELETE FROM merchant;

DELETE FROM user_reminder;
DELETE FROM user_follow_merchant;
DELETE FROM user_favorite;

DELETE FROM chat_message_index;
DELETE FROM chat_session_member;
DELETE FROM chat_group_member;
DELETE FROM chat_group;
DELETE FROM chat_session;
DELETE FROM user_blacklist;
DELETE FROM friendship;
DELETE FROM friend_request;

DELETE FROM meal_activity_feedback;
DELETE FROM meal_activity_member;
DELETE FROM meal_activity;

DELETE FROM payment_record;
DELETE FROM order_item;
DELETE FROM food_order;

DELETE FROM campaign_food;
DELETE FROM campaign_stat;
DELETE FROM marketing_campaign;

DELETE FROM platform_notice;
DELETE FROM recommend_slot;
DELETE FROM user_punishment;
DELETE FROM content_audit;

DELETE FROM stat_merchant_daily;
DELETE FROM stat_daily;
DELETE FROM rec_user_item;
DELETE FROM user_behavior;
DELETE FROM inbox_message;

DELETE FROM user_feed_post;

DELETE FROM sys_user_oauth;
DELETE FROM sys_user_role;
DELETE FROM sys_login_log;
DELETE FROM sys_user;

-- -----------------------------------------------------------------------------
-- A. 用户与权限
-- 密码统一 123456；BCrypt hash 已用项目同款 BCryptPasswordEncoder 验证
-- 注意：taste_tags / dislike_ingredients 是 patch_recommend_tags.sql 追加的列，
--       这里不写入，保持 NULL；需要演示口味推荐时执行文件末尾的可选 UPDATE。
-- -----------------------------------------------------------------------------

INSERT INTO sys_user (
  id, phone, email, password_hash, nickname, avatar_url, gender, school, major, bio,
  privacy_feed, privacy_add_friend, status, role_id, last_login_at, created_at, updated_at, deleted
) VALUES
(1, '13800000001', 'user1@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '平台管理员', '/uploads/demo/avatar1.jpg', 0, '某某大学', '软件工程', '平台运营账号', 1, 1, 1, 3, '2026-11-16 08:30:00', '2026-11-01 08:00:00', '2026-11-16 08:30:00', 0),
(2, '13800000002', 'user2@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '小杨', '/uploads/demo/avatar2.jpg', 1, '某某大学', '软件工程', '爱吃辣，无辣不欢', 1, 1, 1, 1, '2026-11-16 09:10:00', '2026-11-01 09:00:00', '2026-11-16 09:10:00', 0),
(3, '13800000003', 'user3@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '小李', '/uploads/demo/avatar3.jpg', 2, '某某大学', '软件工程', '轻食主义者', 1, 1, 1, 1, '2026-11-16 09:20:00', '2026-11-01 10:00:00', '2026-11-16 09:20:00', 0),
(4, '13800000004', 'user4@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '小王', NULL, 1, '某某大学', '软件工程', '夜宵爱好者', 1, 1, 1, 1, '2026-11-15 21:00:00', '2026-11-02 09:00:00', '2026-11-15 21:00:00', 0),
(5, '13800000005', 'user5@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '小张', '/uploads/demo/avatar5.jpg', 1, '某某大学', '软件工程', '每天都要好好吃饭', 1, 1, 1, 1, '2026-11-15 12:30:00', '2026-11-02 10:00:00', '2026-11-15 12:30:00', 0),
(6, '13800000006', 'user6@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '小陈', '/uploads/demo/avatar6.jpg', 2, '某某大学', '软件工程', NULL, 1, 1, 1, 1, '2026-11-14 19:00:00', '2026-11-03 09:00:00', '2026-11-14 19:00:00', 0),
(7, '13800000007', 'user7@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '刘老板', '/uploads/demo/avatar7.jpg', 1, '某某大学', '软件工程', '一食堂家常菜窗口', 1, 1, 1, 2, '2026-11-16 07:30:00', '2026-11-01 11:00:00', '2026-11-16 07:30:00', 0),
(8, '13800000008', 'user8@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '赵老板', '/uploads/demo/avatar8.jpg', 2, '某某大学', '软件工程', '二食堂轻食沙拉店主', 1, 1, 1, 2, '2026-11-16 08:00:00', '2026-11-02 11:00:00', '2026-11-16 08:00:00', 0),
(9, '13800000009', 'user9@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '孙老板', '/uploads/demo/avatar9.jpg', 1, '某某大学', '软件工程', '南门深夜烧烤', 1, 1, 1, 2, '2026-11-15 22:00:00', '2026-11-05 11:00:00', '2026-11-15 22:00:00', 0),
(10, '13800000010', 'user10@campus.edu', '$2a$10$Ot2rQsqBnEajdhjbht19YOnB6iYLKcN8.177sx1I1Byv0OcInoq.O', '违规用户', '/uploads/demo/avatar10.jpg', 1, '某某大学', '软件工程', '账号已被封禁', 1, 1, 0, 1, '2026-11-13 20:00:00', '2026-11-06 09:00:00', '2026-11-15 10:00:00', 0);

INSERT INTO sys_user_role (id, user_id, role_id, created_at) VALUES
(1, 1, 3, '2026-11-01 08:00:00'),
(2, 2, 1, '2026-11-01 09:00:00'),
(3, 3, 1, '2026-11-01 10:00:00'),
(4, 4, 1, '2026-11-02 09:00:00'),
(5, 5, 1, '2026-11-02 10:00:00'),
(6, 6, 1, '2026-11-03 09:00:00'),
(7, 7, 2, '2026-11-01 11:00:00'),
(8, 8, 2, '2026-11-02 11:00:00'),
(9, 9, 2, '2026-11-05 11:00:00'),
(10, 10, 1, '2026-11-06 09:00:00');

INSERT INTO sys_user_oauth (id, user_id, provider, open_id, union_id, extra_json, created_at) VALUES
(1, 2, 'WECHAT', 'demo_openid_001', 'demo_union_001', '{"scope":"snsapi_userinfo"}', '2026-11-02 09:30:00');

INSERT INTO sys_login_log (id, user_id, login_type, ip, user_agent, success, message, created_at) VALUES
(1, 1, 'PASSWORD', '127.0.0.1', 'Apifox/1.0', 1, '登录成功', '2026-11-16 08:30:00'),
(2, 2, 'PASSWORD', '127.0.0.1', 'Apifox/1.0', 1, '登录成功', '2026-11-16 09:10:00'),
(3, 3, 'PASSWORD', '127.0.0.1', 'Apifox/1.0', 1, '登录成功', '2026-11-16 09:20:00'),
(4, 2, 'PASSWORD', '127.0.0.1', 'Apifox/1.0', 0, '密码错误', '2026-11-15 20:00:00'),
(5, 10, 'PASSWORD', '127.0.0.1', 'Apifox/1.0', 0, '账号已被封禁', '2026-11-15 10:05:00');

-- -----------------------------------------------------------------------------
-- B. 商家与菜品
-- 店铺 1/2 审核通过且营业；店铺 3 待审未开业，其菜品为下架状态，
-- 公开列表（要求 m.audit_status=1 AND m.status=1）不会展示。
-- -----------------------------------------------------------------------------

INSERT INTO merchant (
  id, owner_user_id, shop_name, campus_area, address, contact_phone, logo_url, description,
  license_img_url, audit_status, audit_remark, audited_at, audited_by, status, settled_at,
  created_at, updated_at, deleted
) VALUES
(1, 7, '一食堂·家常菜', '东区食堂', '东区一食堂一层 1 号窗口', '13800000007', '/uploads/demo/merchant1.jpg', '主打家常盖浇饭和面食，分量足。', '/uploads/demo/license1.jpg', 1, '资质齐全，审核通过', '2026-11-02 10:00:00', 1, 1, '2026-11-02 10:00:00', '2026-11-01 11:10:00', '2026-11-02 10:00:00', 0),
(2, 8, '二食堂·轻食沙拉', '西区食堂', '西区二食堂二层 6 号窗口', '13800000008', '/uploads/demo/merchant2.jpg', '低卡轻食、沙拉和鲜榨饮品。', '/uploads/demo/license2.jpg', 1, '资质齐全，审核通过', '2026-11-03 10:00:00', 1, 1, '2026-11-03 10:00:00', '2026-11-02 11:10:00', '2026-11-03 10:00:00', 0),
(3, 9, '南门·深夜烧烤', '南门商圈', '南门步行街 12 号', '13800000009', '/uploads/demo/merchant3.jpg', '夜间烧烤，正在等待平台审核。', '/uploads/demo/license3.jpg', 0, NULL, NULL, NULL, 0, NULL, '2026-11-05 11:10:00', '2026-11-05 11:10:00', 0);

INSERT INTO merchant_qualification (id, merchant_id, doc_type, doc_url, created_at) VALUES
(1, 1, 'BUSINESS_LICENSE', '/uploads/demo/license1.jpg', '2026-11-01 11:20:00'),
(2, 2, 'BUSINESS_LICENSE', '/uploads/demo/license2.jpg', '2026-11-02 11:20:00'),
(3, 3, 'BUSINESS_LICENSE', '/uploads/demo/license3.jpg', '2026-11-05 11:20:00');

INSERT INTO food_category (id, parent_id, name, sort_no, status, created_at) VALUES
(1, 0, '中餐', 1, 1, '2026-11-01 08:00:00'),
(2, 0, '西餐', 2, 1, '2026-11-01 08:00:00'),
(3, 0, '小吃', 3, 1, '2026-11-01 08:00:00'),
(4, 0, '饮品', 4, 1, '2026-11-01 08:00:00'),
(5, 1, '盖浇饭', 1, 1, '2026-11-01 08:10:00'),
(6, 1, '面食', 2, 1, '2026-11-01 08:10:00');

INSERT INTO food_item (
  id, merchant_id, category_id, name, food_type, price, stock, description, cover_url,
  avg_score, review_count, sales_count, recommend_tag, status, created_at, updated_at, deleted
) VALUES
(1, 1, 5, '番茄炒蛋盖饭', '中餐', 12.00, 100, '家常番茄炒蛋，酸甜下饭。', '/uploads/demo/food1.jpg', 4.50, 2, 88, '招牌', 1, '2026-11-01 12:00:00', '2026-11-16 09:00:00', 0),
(2, 1, 5, '红烧肉盖饭', '中餐', 16.00, 80, '肥瘦相间，配时蔬和卤蛋。', '/uploads/demo/food2.jpg', 4.50, 2, 120, '热门', 1, '2026-11-01 12:10:00', '2026-11-16 09:00:00', 0),
(3, 1, 6, '香辣鸡腿面', '中餐', 14.00, 60, '现煮面条配香辣鸡腿。', '/uploads/demo/food3.jpg', 4.00, 2, 66, '新品', 1, '2026-11-01 12:20:00', '2026-11-16 09:00:00', 0),
(4, 1, 3, '凉拌黄瓜', '小吃', 6.00, 40, '清爽解腻的小菜。', '/uploads/demo/food4.jpg', 4.00, 1, 25, NULL, 2, '2026-11-01 12:30:00', '2026-11-10 09:00:00', 0),
(5, 2, 2, '牛油果鸡胸沙拉', '西餐', 22.00, 50, '低卡高蛋白，配油醋汁。', '/uploads/demo/food5.jpg', 5.00, 1, 42, '招牌', 1, '2026-11-02 12:00:00', '2026-11-16 09:00:00', 0),
(6, 2, 2, '意式肉酱面', '西餐', 20.00, 40, '番茄肉酱，酸甜浓郁。', '/uploads/demo/food6.jpg', 0.00, 0, 37, NULL, 1, '2026-11-02 12:10:00', '2026-11-16 09:00:00', 0),
(7, 2, 4, '鲜榨橙汁', '饮品', 9.00, 100, '当天鲜榨，不加水。', '/uploads/demo/food7.jpg', 5.00, 1, 150, '热销', 1, '2026-11-02 12:20:00', '2026-11-16 09:00:00', 0),
(8, 2, 2, '提拉米苏', '西餐', 15.00, 0, '新品研发中，暂未上架。', '/uploads/demo/food8.jpg', 0.00, 0, 12, NULL, 0, '2026-11-02 12:30:00', '2026-11-10 09:00:00', 0),
(9, 3, 3, '炭烤羊肉串', '小吃', 5.00, 200, '现烤羊肉串，孜然味足。', '/uploads/demo/food9.jpg', 0.00, 0, 0, NULL, 2, '2026-11-05 12:00:00', '2026-11-05 12:00:00', 0),
(10, 3, 3, '烤鸡翅', '小吃', 6.00, 150, '蜜汁烤翅，外焦里嫩。', '/uploads/demo/food10.jpg', 0.00, 0, 0, NULL, 2, '2026-11-05 12:10:00', '2026-11-05 12:10:00', 0),
(11, 3, 3, '烤茄子', '小吃', 8.00, 80, '蒜蓉烤茄子。', '/uploads/demo/food11.jpg', 0.00, 0, 0, NULL, 2, '2026-11-05 12:20:00', '2026-11-05 12:20:00', 0),
(12, 3, 4, '冰镇酸梅汤', '饮品', 6.00, 120, '解腻解辣。', '/uploads/demo/food12.jpg', 0.00, 0, 0, NULL, 2, '2026-11-05 12:30:00', '2026-11-05 12:30:00', 0);

INSERT INTO food_image (id, food_id, url, sort_no, created_at) VALUES
(1, 1, '/uploads/demo/food1_1.jpg', 0, '2026-11-01 12:05:00'),
(2, 2, '/uploads/demo/food2_1.jpg', 0, '2026-11-01 12:15:00'),
(3, 3, '/uploads/demo/food3_1.jpg', 0, '2026-11-01 12:25:00'),
(4, 4, '/uploads/demo/food4_1.jpg', 0, '2026-11-01 12:35:00'),
(5, 5, '/uploads/demo/food5_1.jpg', 0, '2026-11-02 12:05:00'),
(6, 6, '/uploads/demo/food6_1.jpg', 0, '2026-11-02 12:15:00'),
(7, 7, '/uploads/demo/food7_1.jpg', 0, '2026-11-02 12:25:00'),
(8, 8, '/uploads/demo/food8_1.jpg', 0, '2026-11-02 12:35:00'),
(9, 9, '/uploads/demo/food9_1.jpg', 0, '2026-11-05 12:05:00'),
(10, 10, '/uploads/demo/food10_1.jpg', 0, '2026-11-05 12:15:00'),
(11, 11, '/uploads/demo/food11_1.jpg', 0, '2026-11-05 12:25:00'),
(12, 12, '/uploads/demo/food12_1.jpg', 0, '2026-11-05 12:35:00');

-- -----------------------------------------------------------------------------
-- C. 评价互动
-- audit_status：1 通过 / 0 待审 / 2 拒绝；like_count 与 review_like 行数一致
-- -----------------------------------------------------------------------------

INSERT INTO food_review (
  id, user_id, food_id, merchant_id, order_id, score, content, like_count,
  audit_status, status, created_at, updated_at
) VALUES
(1, 2, 1, 1, NULL, 5, '番茄很入味，米饭给得很足。', 2, 1, 1, '2026-11-02 12:10:00', '2026-11-02 12:10:00'),
(2, 3, 1, 1, NULL, 4, '味道不错，就是稍微有点咸。', 1, 1, 1, '2026-11-03 12:20:00', '2026-11-03 12:20:00'),
(3, 4, 2, 1, NULL, 5, '红烧肉软糯，配菜也新鲜。', 2, 1, 1, '2026-11-04 18:30:00', '2026-11-04 18:30:00'),
(4, 5, 2, 1, NULL, 4, '分量很大，性价比高。', 1, 1, 1, '2026-11-05 12:15:00', '2026-11-05 12:15:00'),
(5, 6, 2, 1, NULL, 4, '整体不错，排队有点久。', 0, 0, 1, '2026-11-06 12:40:00', '2026-11-06 12:40:00'),
(6, 2, 3, 1, NULL, 4, '鸡腿很嫩，面条筋道。', 1, 1, 1, '2026-11-07 12:05:00', '2026-11-07 12:05:00'),
(7, 3, 3, 1, NULL, 4, '香辣口味很过瘾。', 0, 1, 1, '2026-11-08 19:00:00', '2026-11-08 19:00:00'),
(8, 4, 4, 1, NULL, 4, '清爽解腻，适合夏天。', 0, 1, 1, '2026-11-09 12:30:00', '2026-11-09 12:30:00'),
(9, 5, 5, 2, NULL, 5, '鸡胸肉不柴，酱汁好吃。', 1, 1, 1, '2026-11-09 18:20:00', '2026-11-09 18:20:00'),
(10, 6, 6, 2, NULL, 4, '肉酱给得很足。', 0, 2, 1, '2026-11-10 12:10:00', '2026-11-10 12:10:00'),
(11, 2, 7, 2, NULL, 5, '真的是鲜榨的，很甜。', 0, 1, 1, '2026-11-12 15:00:00', '2026-11-12 15:00:00'),
(12, 3, 8, 2, NULL, 3, '甜度有点高。', 0, 0, 1, '2026-11-13 16:00:00', '2026-11-13 16:00:00');

INSERT INTO review_image (id, review_id, url, sort_no, created_at) VALUES
(1, 3, '/uploads/demo/review3_1.jpg', 0, '2026-11-04 18:31:00'),
(2, 3, '/uploads/demo/review3_2.jpg', 1, '2026-11-04 18:31:00'),
(3, 6, '/uploads/demo/review6_1.jpg', 0, '2026-11-07 12:06:00'),
(4, 6, '/uploads/demo/review6_2.jpg', 1, '2026-11-07 12:06:00'),
(5, 9, '/uploads/demo/review9_1.jpg', 0, '2026-11-09 18:21:00'),
(6, 9, '/uploads/demo/review9_2.jpg', 1, '2026-11-09 18:21:00');

INSERT INTO review_like (id, review_id, user_id, created_at) VALUES
(1, 1, 3, '2026-11-02 13:00:00'),
(2, 1, 4, '2026-11-02 14:00:00'),
(3, 2, 2, '2026-11-03 13:10:00'),
(4, 3, 5, '2026-11-04 19:00:00'),
(5, 3, 6, '2026-11-04 19:05:00'),
(6, 4, 2, '2026-11-05 13:00:00'),
(7, 6, 6, '2026-11-07 13:00:00'),
(8, 9, 2, '2026-11-09 19:00:00');

INSERT INTO review_reply (
  id, review_id, user_id, reply_to_uid, content, from_merchant, audit_status, created_at
) VALUES
(1, 1, 3, 2, '我也觉得这家番茄炒蛋好吃。', 0, 1, '2026-11-02 13:20:00'),
(2, 1, 7, 2, '谢谢支持，欢迎常来！', 1, 1, '2026-11-02 14:30:00'),
(3, 3, 5, 4, '下次我也去试试红烧肉。', 0, 1, '2026-11-04 20:00:00'),
(4, 6, 7, 2, '鸡腿每天现卤，欢迎再来。', 1, 1, '2026-11-07 14:00:00'),
(5, 9, 6, 5, '沙拉酱可以再多给一点吗？', 0, 1, '2026-11-09 20:00:00');

INSERT INTO content_audit (
  id, content_type, content_id, submit_uid, title, snapshot, status,
  auditor_id, audit_remark, audited_at, created_at
) VALUES
(1, 'REVIEW', 5, 6, '红烧肉盖饭评价', '整体不错，排队有点久。', 0, NULL, NULL, NULL, '2026-11-06 12:41:00'),
(2, 'REVIEW', 1, 2, '番茄炒蛋盖饭评价', '番茄很入味，米饭给得很足。', 1, 1, '内容正常', '2026-11-02 12:30:00', '2026-11-02 12:11:00'),
(3, 'REVIEW', 10, 6, '意式肉酱面评价', '肉酱给得很足。', 2, 1, '疑似广告内容', '2026-11-10 12:30:00', '2026-11-10 12:11:00');

-- -----------------------------------------------------------------------------
-- D. 收藏与提醒
-- -----------------------------------------------------------------------------

INSERT INTO user_favorite (id, user_id, target_type, target_id, created_at) VALUES
(1, 2, 1, 1, '2026-11-03 12:00:00'),
(2, 2, 1, 3, '2026-11-07 13:00:00'),
(3, 3, 1, 2, '2026-11-04 12:00:00'),
(4, 3, 2, 1, '2026-11-04 12:10:00'),
(5, 4, 1, 5, '2026-11-09 19:00:00'),
(6, 4, 2, 2, '2026-11-09 19:10:00'),
(7, 5, 1, 7, '2026-11-12 16:00:00'),
(8, 6, 2, 3, '2026-11-13 17:00:00');

INSERT INTO user_follow_merchant (id, user_id, merchant_id, created_at) VALUES
(1, 2, 1, '2026-11-03 12:05:00'),
(2, 3, 1, '2026-11-04 12:05:00'),
(3, 4, 2, '2026-11-09 19:05:00'),
(4, 5, 2, '2026-11-12 16:05:00');

INSERT INTO user_reminder (
  id, user_id, remind_type, channel, enabled, ref_type, ref_id, created_at, updated_at
) VALUES
(1, 2, 'CAMPAIGN', 'INBOX', 1, NULL, NULL, '2026-11-01 09:00:00', '2026-11-01 09:00:00'),
(2, 2, 'FRIEND_FEED', 'INBOX', 1, NULL, NULL, '2026-11-01 09:00:00', '2026-11-01 09:00:00'),
(3, 2, 'ORDER', 'INBOX', 1, NULL, NULL, '2026-11-01 09:00:00', '2026-11-01 09:00:00'),
(4, 3, 'CAMPAIGN', 'INBOX', 1, NULL, NULL, '2026-11-01 10:00:00', '2026-11-01 10:00:00'),
(5, 3, 'FRIEND_FEED', 'INBOX', 1, NULL, NULL, '2026-11-01 10:00:00', '2026-11-01 10:00:00'),
(6, 3, 'ORDER', 'INBOX', 1, NULL, NULL, '2026-11-01 10:00:00', '2026-11-01 10:00:00'),
(7, 4, 'CAMPAIGN', 'INBOX', 1, NULL, NULL, '2026-11-02 09:00:00', '2026-11-02 09:00:00'),
(8, 4, 'FRIEND_FEED', 'INBOX', 1, NULL, NULL, '2026-11-02 09:00:00', '2026-11-02 09:00:00'),
(9, 4, 'ORDER', 'INBOX', 1, NULL, NULL, '2026-11-02 09:00:00', '2026-11-02 09:00:00'),
(10, 5, 'CAMPAIGN', 'INBOX', 1, NULL, NULL, '2026-11-02 10:00:00', '2026-11-02 10:00:00'),
(11, 5, 'FRIEND_FEED', 'INBOX', 1, NULL, NULL, '2026-11-02 10:00:00', '2026-11-02 10:00:00'),
(12, 5, 'ORDER', 'INBOX', 0, NULL, NULL, '2026-11-02 10:00:00', '2026-11-10 09:00:00'),
(13, 6, 'CAMPAIGN', 'INBOX', 0, NULL, NULL, '2026-11-03 09:00:00', '2026-11-11 09:00:00'),
(14, 6, 'FRIEND_FEED', 'INBOX', 1, NULL, NULL, '2026-11-03 09:00:00', '2026-11-03 09:00:00'),
(15, 6, 'ORDER', 'INBOX', 1, NULL, NULL, '2026-11-03 09:00:00', '2026-11-03 09:00:00');

-- -----------------------------------------------------------------------------
-- E. 社交：好友 / 黑名单 / 会话 / 群聊
-- 好友关系双向各一行；用户 2 拉黑用户 4
-- -----------------------------------------------------------------------------

INSERT INTO friend_request (id, from_user_id, to_user_id, message, status, created_at, handled_at) VALUES
(1, 2, 6, '周末一起探店吗？', 0, '2026-11-15 10:00:00', NULL),
(2, 5, 2, '一起吃饭吧', 1, '2026-11-07 09:00:00', '2026-11-07 10:00:00'),
(3, 4, 2, '加个好友', 2, '2026-11-08 09:00:00', '2026-11-08 12:00:00');

INSERT INTO friendship (id, user_id, friend_id, remark, created_at) VALUES
(1, 2, 3, '小李', '2026-11-08 10:00:00'),
(2, 3, 2, '小杨', '2026-11-08 10:00:00'),
(3, 2, 5, '小张', '2026-11-08 11:00:00'),
(4, 5, 2, '小杨', '2026-11-08 11:00:00'),
(5, 3, 6, '小陈', '2026-11-09 09:00:00'),
(6, 6, 3, '小李', '2026-11-09 09:00:00');

INSERT INTO user_blacklist (id, user_id, blocked_uid, created_at) VALUES
(1, 2, 4, '2026-11-08 13:00:00');

INSERT INTO chat_session (
  id, session_type, peer_user_id, group_id, last_msg_preview, last_msg_at, created_at, updated_at
) VALUES
(1, 1, 3, NULL, '明天中午一起吃饭吗？', '2026-11-14 10:03:00', '2026-11-13 09:00:00', '2026-11-14 10:03:00'),
(2, 1, 5, NULL, '好的，老地方见。', '2026-11-15 11:32:00', '2026-11-15 11:30:00', '2026-11-15 11:32:00');

INSERT INTO chat_session_member (id, session_id, user_id, unread_count, joined_at) VALUES
(1, 1, 2, 0, '2026-11-13 09:00:00'),
(2, 1, 3, 2, '2026-11-13 09:00:00'),
(3, 2, 2, 2, '2026-11-15 11:30:00'),
(4, 2, 5, 0, '2026-11-15 11:30:00');

INSERT INTO chat_message_index (id, mongo_msg_id, session_id, from_user_id, msg_type, created_at) VALUES
(1, 'demo_msg_0001', 1, 2, 'TEXT', '2026-11-14 10:00:00'),
(2, 'demo_msg_0002', 1, 3, 'TEXT', '2026-11-14 10:02:00'),
(3, 'demo_msg_0003', 1, 2, 'IMAGE', '2026-11-14 10:03:00'),
(4, 'demo_msg_0004', 2, 2, 'TEXT', '2026-11-15 11:30:00'),
(5, 'demo_msg_0005', 2, 5, 'TEXT', '2026-11-15 11:31:00'),
(6, 'demo_msg_0006', 2, 5, 'IMAGE', '2026-11-15 11:32:00');

INSERT INTO chat_group (
  id, name, owner_user_id, avatar_url, notice, max_members, status, created_at
) VALUES
(1, '校园美食拼单群', 2, '/uploads/demo/group1.jpg', '文明交流，禁止广告。', 200, 1, '2026-11-05 10:00:00');

INSERT INTO chat_group_member (id, group_id, user_id, role, nickname_in_group, joined_at) VALUES
(1, 1, 2, 2, '群主小杨', '2026-11-05 10:00:00'),
(2, 1, 3, 0, '小李', '2026-11-05 10:10:00'),
(3, 1, 5, 0, '小张', '2026-11-05 10:20:00');

-- -----------------------------------------------------------------------------
-- F. 拼单约饭
-- current_members 与 meal_activity_member 中 status=1 的行数一致
-- 活动 1/2 改到 11-18，避免演示时已经过期
-- -----------------------------------------------------------------------------

INSERT INTO meal_activity (
  id, creator_id, title, meet_time, meet_place, food_type, related_food_id, related_merchant_id,
  max_members, current_members, description, signup_deadline, status, created_at, updated_at
) VALUES
(1, 2, '周三中午一食堂拼单', '2026-11-18 12:00:00', '东区一食堂门口', '中餐', 1, 1, 6, 3, '一起吃番茄炒蛋盖饭，AA。', '2026-11-18 11:30:00', 0, '2026-11-14 09:00:00', '2026-11-15 09:00:00'),
(2, 3, '周三晚轻食局', '2026-11-18 18:30:00', '西区二食堂二层', '西餐', 5, 2, 3, 3, '轻食沙拉，名额已满。', '2026-11-18 17:00:00', 1, '2026-11-13 10:00:00', '2026-11-15 10:00:00'),
(3, 4, '上周五烧烤局', '2026-11-13 19:00:00', '南门步行街', '小吃', 9, 3, 5, 2, '已经结束的烧烤拼单。', '2026-11-13 17:00:00', 3, '2026-11-10 09:00:00', '2026-11-13 21:00:00');

INSERT INTO meal_activity_member (id, activity_id, user_id, role, status, joined_at) VALUES
(1, 1, 2, 1, 1, '2026-11-14 09:00:00'),
(2, 1, 3, 0, 1, '2026-11-14 10:00:00'),
(3, 1, 6, 0, 1, '2026-11-15 09:00:00'),
(4, 2, 3, 1, 1, '2026-11-13 10:00:00'),
(5, 2, 2, 0, 1, '2026-11-13 11:00:00'),
(6, 2, 4, 0, 1, '2026-11-15 10:00:00'),
(7, 3, 4, 1, 1, '2026-11-10 09:00:00'),
(8, 3, 2, 0, 1, '2026-11-10 10:00:00');

INSERT INTO meal_activity_feedback (id, activity_id, from_user_id, to_user_id, score, content, created_at) VALUES
(1, 3, 2, 4, 5, '组织得不错，烧烤很好吃。', '2026-11-13 21:30:00'),
(2, 3, 4, 2, 4, '下次还来，就是有点远。', '2026-11-13 21:40:00');

-- -----------------------------------------------------------------------------
-- G. 订单与支付
-- total_amount = order_item.amount 合计；pay_amount = total_amount - discount_amount
-- -----------------------------------------------------------------------------

INSERT INTO food_order (
  id, order_no, user_id, merchant_id, total_amount, discount_amount, pay_amount, campaign_id,
  status, delivery_type, delivery_address, remark, paid_at, accepted_at, completed_at,
  cancelled_at, cancel_reason, created_at, updated_at
) VALUES
(1, '2026111400001', 2, 1, 24.00, 4.00, 20.00, 1, 'PAID', 1, NULL, '不要香菜', '2026-11-14 12:05:00', NULL, NULL, NULL, NULL, '2026-11-14 12:00:00', '2026-11-14 12:05:00'),
(2, '2026111500001', 3, 2, 31.00, 0.00, 31.00, NULL, 'ACCEPTED', 1, NULL, NULL, '2026-11-15 11:32:00', '2026-11-15 11:35:00', NULL, NULL, NULL, '2026-11-15 11:30:00', '2026-11-15 11:35:00'),
(3, '2026111000001', 4, 1, 30.00, 0.00, 30.00, NULL, 'COMPLETED', 1, NULL, NULL, '2026-11-10 18:02:00', '2026-11-10 18:05:00', '2026-11-10 18:40:00', NULL, NULL, '2026-11-10 18:00:00', '2026-11-10 18:40:00'),
(4, '2026111200001', 5, 2, 20.00, 0.00, 20.00, NULL, 'CANCELLED', 1, NULL, NULL, NULL, NULL, NULL, '2026-11-12 12:10:00', '临时有事', '2026-11-12 12:00:00', '2026-11-12 12:10:00');

INSERT INTO order_item (id, order_id, food_id, food_name, unit_price, quantity, amount) VALUES
(1, 1, 1, '番茄炒蛋盖饭', 12.00, 2, 24.00),
(2, 2, 5, '牛油果鸡胸沙拉', 22.00, 1, 22.00),
(3, 2, 7, '鲜榨橙汁', 9.00, 1, 9.00),
(4, 3, 2, '红烧肉盖饭', 16.00, 1, 16.00),
(5, 3, 3, '香辣鸡腿面', 14.00, 1, 14.00),
(6, 4, 6, '意式肉酱面', 20.00, 1, 20.00);

INSERT INTO payment_record (
  id, order_id, order_no, pay_channel, trade_no, amount, status, callback_raw, paid_at, created_at
) VALUES
(1, 1, '2026111400001', 'MOCK', 'MOCK202611140001', 20.00, 1, '{"result":"SUCCESS"}', '2026-11-14 12:05:00', '2026-11-14 12:04:00'),
(2, 2, '2026111500001', 'MOCK', 'MOCK202611150001', 31.00, 1, '{"result":"SUCCESS"}', '2026-11-15 11:32:00', '2026-11-15 11:31:00'),
(3, 3, '2026111000001', 'MOCK', 'MOCK202611100001', 30.00, 1, '{"result":"SUCCESS"}', '2026-11-10 18:02:00', '2026-11-10 18:01:00');

-- -----------------------------------------------------------------------------
-- H. 营销活动
-- 活动 2 已结束且没有订单使用，统计改为 0，避免与订单对不上
-- -----------------------------------------------------------------------------

INSERT INTO marketing_campaign (
  id, merchant_id, title, campaign_type, rule_json, start_at, end_at, status, created_at, updated_at
) VALUES
(1, 1, '满20减4', 'FULL_REDUCE', '{"threshold":20,"reduce":4}', '2026-11-10 00:00:00', '2026-11-20 23:59:00', 1, '2026-11-09 10:00:00', '2026-11-09 10:00:00'),
(2, 2, '全场9折', 'DISCOUNT', '{"discount":0.9}', '2026-11-01 00:00:00', '2026-11-12 23:59:00', 2, '2026-11-01 09:00:00', '2026-11-13 00:00:00');

INSERT INTO campaign_food (id, campaign_id, food_id) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 2, 5),
(4, 2, 7);

INSERT INTO campaign_stat (
  id, campaign_id, participant_cnt, order_cnt, sales_amount, stat_date, updated_at
) VALUES
(1, 1, 2, 1, 20.00, '2026-11-14', '2026-11-14 23:59:00'),
(2, 2, 0, 0, 0.00, '2026-11-11', '2026-11-11 23:59:00');

-- -----------------------------------------------------------------------------
-- I. 审核 / 公告 / 处罚 / 推荐位
-- -----------------------------------------------------------------------------

INSERT INTO user_punishment (
  id, user_id, action, reason, operator_id, expire_at, created_at
) VALUES
(1, 10, 'BAN', '多次发布违规广告内容', 1, NULL, '2026-11-15 10:00:00');

INSERT INTO platform_notice (
  id, title, content, publish_status, publisher_id, published_at, created_at, updated_at
) VALUES
(1, '平台上线公告', '校园美食平台正式上线，欢迎同学们使用。', 1, 1, '2026-11-01 09:00:00', '2026-11-01 08:30:00', '2026-11-01 09:00:00'),
(2, '双十一食堂优惠活动', '11 月 10 日至 16 日，多家店铺满减活动进行中。', 1, 1, '2026-11-08 10:00:00', '2026-11-08 09:30:00', '2026-11-08 10:00:00'),
(3, '期末推荐算法调整说明', '草稿：期末期间推荐权重调整方案。', 0, 1, NULL, '2026-11-15 15:00:00', '2026-11-15 15:00:00');

INSERT INTO recommend_slot (
  id, slot_code, title, target_type, target_id, link_url, image_url, sort_no, status,
  start_at, end_at, created_at
) VALUES
(1, 'HOME_BANNER', '今日招牌', 'FOOD', 1, NULL, '/uploads/demo/banner1.jpg', 1, 1, '2026-11-01 00:00:00', '2026-11-20 23:59:00', '2026-11-01 08:00:00'),
(2, 'HOME_BANNER', '轻食新店', 'MERCHANT', 2, NULL, '/uploads/demo/banner2.jpg', 2, 1, '2026-11-01 00:00:00', '2026-11-20 23:59:00', '2026-11-02 08:00:00'),
(3, 'HOME_BANNER', '更多活动', 'URL', NULL, 'https://example.com/campus-food', '/uploads/demo/banner3.jpg', 3, 1, '2026-11-01 00:00:00', '2026-11-20 23:59:00', '2026-11-03 08:00:00');

-- -----------------------------------------------------------------------------
-- J. 站内消息 / 用户行为 / 统计
-- -----------------------------------------------------------------------------

INSERT INTO inbox_message (
  id, user_id, msg_type, title, content, ref_type, ref_id, is_read, created_at
) VALUES
(1, 2, 'SYSTEM', '欢迎使用校园美食平台', '完善资料后可以获得更准确的推荐。', NULL, NULL, 1, '2026-11-01 09:10:00'),
(2, 2, 'ORDER', '订单已支付', '订单 2026111400001 已支付成功。', 'ORDER', 1, 1, '2026-11-14 12:05:00'),
(3, 3, 'CAMPAIGN', '满20减4活动进行中', '一食堂·家常菜满 20 减 4。', 'CAMPAIGN', 1, 0, '2026-11-10 09:00:00'),
(4, 4, 'ORDER', '订单已完成', '订单 2026111000001 已完成，欢迎评价。', 'ORDER', 3, 0, '2026-11-10 18:40:00');

INSERT INTO user_behavior (
  id, user_id, behavior_type, food_id, merchant_id, score, extra_json, created_at
) VALUES
(1, 2, 'VIEW', 1, 1, 1.00, '{"source":"demo"}', '2026-11-10 12:00:00'),
(2, 2, 'FAV', 1, 1, 2.00, '{"source":"demo"}', '2026-11-10 12:05:00'),
(3, 2, 'ORDER', 1, 1, 3.00, '{"source":"demo"}', '2026-11-14 12:00:00'),
(4, 2, 'SCORE', 1, 1, 5.00, '{"source":"demo"}', '2026-11-14 12:10:00'),
(5, 3, 'VIEW', 2, 1, 1.00, '{"source":"demo"}', '2026-11-11 12:00:00'),
(6, 3, 'FAV', 3, 1, 2.00, '{"source":"demo"}', '2026-11-11 12:05:00'),
(7, 3, 'ORDER', 2, 1, 3.00, '{"source":"demo"}', '2026-11-15 11:30:00'),
(8, 3, 'SCORE', 2, 1, 4.00, '{"source":"demo"}', '2026-11-15 11:40:00'),
(9, 4, 'VIEW', 5, 2, 1.00, '{"source":"demo"}', '2026-11-12 12:00:00'),
(10, 4, 'FAV', 5, 2, 2.00, '{"source":"demo"}', '2026-11-12 12:05:00'),
(11, 4, 'ORDER', 3, 1, 3.00, '{"source":"demo"}', '2026-11-10 18:00:00'),
(12, 4, 'SCORE', 3, 1, 4.00, '{"source":"demo"}', '2026-11-10 18:50:00'),
(13, 5, 'VIEW', 7, 2, 1.00, '{"source":"demo"}', '2026-11-13 12:00:00'),
(14, 5, 'FAV', 7, 2, 2.00, '{"source":"demo"}', '2026-11-13 12:05:00'),
(15, 5, 'ORDER', 5, 2, 3.00, '{"source":"demo"}', '2026-11-16 11:30:00'),
(16, 5, 'SCORE', 5, 2, 5.00, '{"source":"demo"}', '2026-11-16 11:40:00'),
(17, 6, 'VIEW', 2, 1, 1.00, '{"source":"demo"}', '2026-11-14 12:00:00'),
(18, 6, 'FAV', 1, 1, 2.00, '{"source":"demo"}', '2026-11-14 12:05:00'),
(19, 6, 'ORDER', 6, 2, 3.00, '{"source":"demo"}', '2026-11-14 19:00:00'),
(20, 6, 'SCORE', 6, 2, 4.00, '{"source":"demo"}', '2026-11-14 19:10:00');

INSERT INTO stat_daily (
  id, stat_date, user_total, user_new, dau, order_cnt, gmv, review_cnt, merchant_active, created_at
) VALUES
(1, '2026-11-10', 10, 0, 5, 1, 30.00, 1, 2, '2026-11-10 23:59:00'),
(2, '2026-11-11', 10, 0, 6, 0, 0.00, 0, 2, '2026-11-11 23:59:00'),
(3, '2026-11-12', 10, 0, 5, 1, 0.00, 1, 2, '2026-11-12 23:59:00'),
(4, '2026-11-13', 10, 0, 7, 0, 0.00, 1, 2, '2026-11-13 23:59:00'),
(5, '2026-11-14', 10, 0, 6, 1, 20.00, 0, 2, '2026-11-14 23:59:00'),
(6, '2026-11-15', 10, 0, 8, 1, 31.00, 0, 2, '2026-11-15 23:59:00'),
(7, '2026-11-16', 10, 0, 7, 0, 0.00, 0, 2, '2026-11-16 23:59:00');

INSERT INTO stat_merchant_daily (
  id, stat_date, merchant_id, order_cnt, sales_amount, avg_score, review_cnt, created_at
) VALUES
(1, '2026-11-10', 1, 1, 30.00, 4.00, 1, '2026-11-10 23:59:00'),
(2, '2026-11-10', 2, 0, 0.00, 5.00, 0, '2026-11-10 23:59:00'),
(3, '2026-11-10', 3, 0, 0.00, NULL, 0, '2026-11-10 23:59:00'),
(4, '2026-11-11', 1, 0, 0.00, 4.00, 1, '2026-11-11 23:59:00'),
(5, '2026-11-11', 2, 0, 0.00, 5.00, 0, '2026-11-11 23:59:00'),
(6, '2026-11-11', 3, 0, 0.00, NULL, 0, '2026-11-11 23:59:00'),
(7, '2026-11-12', 1, 0, 0.00, 4.00, 0, '2026-11-12 23:59:00'),
(8, '2026-11-12', 2, 0, 0.00, 5.00, 0, '2026-11-12 23:59:00'),
(9, '2026-11-12', 3, 0, 0.00, NULL, 0, '2026-11-12 23:59:00'),
(10, '2026-11-13', 1, 0, 0.00, 4.00, 0, '2026-11-13 23:59:00'),
(11, '2026-11-13', 2, 0, 0.00, 5.00, 0, '2026-11-13 23:59:00'),
(12, '2026-11-13', 3, 0, 0.00, NULL, 0, '2026-11-13 23:59:00'),
(13, '2026-11-14', 1, 1, 20.00, 4.50, 1, '2026-11-14 23:59:00'),
(14, '2026-11-14', 2, 0, 0.00, 5.00, 0, '2026-11-14 23:59:00'),
(15, '2026-11-14', 3, 0, 0.00, NULL, 0, '2026-11-14 23:59:00'),
(16, '2026-11-15', 1, 0, 0.00, 4.50, 0, '2026-11-15 23:59:00'),
(17, '2026-11-15', 2, 1, 31.00, 5.00, 0, '2026-11-15 23:59:00'),
(18, '2026-11-15', 3, 0, 0.00, NULL, 0, '2026-11-15 23:59:00'),
(19, '2026-11-16', 1, 0, 0.00, 4.50, 0, '2026-11-16 23:59:00'),
(20, '2026-11-16', 2, 0, 0.00, 5.00, 0, '2026-11-16 23:59:00'),
(21, '2026-11-16', 3, 0, 0.00, NULL, 0, '2026-11-16 23:59:00');

-- -----------------------------------------------------------------------------
-- K. 用户动态
-- -----------------------------------------------------------------------------

INSERT INTO user_feed_post (
  id, user_id, content, food_id, image_urls, created_at, privacy, like_count
) VALUES
(1, 2, '今天的一食堂番茄炒蛋盖饭，绝了。', 1, '/uploads/demo/feed1.jpg,/uploads/demo/feed2.jpg', '2026-11-14 12:30:00', 1, 3),
(2, 3, '轻食沙拉打卡，继续保持。', 5, NULL, '2026-11-15 12:00:00', 1, 1),
(3, 4, '深夜烧烤走起。', 9, NULL, '2026-11-15 21:30:00', 1, 0),
(4, 5, '一个人也要好好吃饭。', 6, NULL, '2026-11-13 12:30:00', 0, 0),
(5, 6, '分享一家宝藏小店。', NULL, '/uploads/demo/feed3.jpg', '2026-11-12 18:00:00', 1, 2);

COMMIT;

-- =============================================================================
-- 可选：口味标签（仅当已执行 sql/patch_recommend_tags.sql 时取消注释）
-- 不取消注释也能跑，只是推荐接口没有口味标签，会走热门回退。
-- =============================================================================
-- UPDATE sys_user SET taste_tags = '辣,咸鲜', dislike_ingredients = '香菜,折耳根' WHERE id = 1;
-- UPDATE sys_user SET taste_tags = '辣,咸鲜', dislike_ingredients = '香菜' WHERE id = 2;
-- UPDATE sys_user SET taste_tags = '清淡,鲜', dislike_ingredients = '肥肉,动物内脏' WHERE id = 3;
-- UPDATE sys_user SET taste_tags = '咸鲜,香辣', dislike_ingredients = '香菜' WHERE id = 4;
-- UPDATE sys_user SET taste_tags = '甜,清淡', dislike_ingredients = '内脏' WHERE id = 5;
-- UPDATE sys_user SET taste_tags = '清淡,甜', dislike_ingredients = '辣' WHERE id = 6;
-- UPDATE food_item SET taste_tags = '咸鲜,微甜', ingredients = '番茄,鸡蛋,米饭' WHERE id = 1;
-- UPDATE food_item SET taste_tags = '咸甜,浓郁', ingredients = '五花肉,米饭,青菜' WHERE id = 2;
-- UPDATE food_item SET taste_tags = '香辣,咸鲜', ingredients = '鸡腿,面条,辣椒' WHERE id = 3;
-- UPDATE food_item SET taste_tags = '清爽,酸', ingredients = '黄瓜,蒜,醋' WHERE id = 4;
-- UPDATE food_item SET taste_tags = '清淡,鲜', ingredients = '鸡胸肉,牛油果,生菜' WHERE id = 5;
-- UPDATE food_item SET taste_tags = '酸甜,浓郁', ingredients = '番茄,肉酱,面条' WHERE id = 6;
-- UPDATE food_item SET taste_tags = '甜,鲜', ingredients = '橙子' WHERE id = 7;

/* =============================================================================
   验证 SQL（默认注释，需要时整段取消注释执行）
   =============================================================================

SELECT 'sys_user' AS t, COUNT(*) AS c FROM sys_user
UNION ALL SELECT 'sys_user_role', COUNT(*) FROM sys_user_role
UNION ALL SELECT 'merchant', COUNT(*) FROM merchant
UNION ALL SELECT 'merchant_qualification', COUNT(*) FROM merchant_qualification
UNION ALL SELECT 'food_category', COUNT(*) FROM food_category
UNION ALL SELECT 'food_item', COUNT(*) FROM food_item
UNION ALL SELECT 'food_image', COUNT(*) FROM food_image
UNION ALL SELECT 'food_review', COUNT(*) FROM food_review
UNION ALL SELECT 'review_image', COUNT(*) FROM review_image
UNION ALL SELECT 'review_like', COUNT(*) FROM review_like
UNION ALL SELECT 'review_reply', COUNT(*) FROM review_reply
UNION ALL SELECT 'food_order', COUNT(*) FROM food_order
UNION ALL SELECT 'order_item', COUNT(*) FROM order_item
UNION ALL SELECT 'payment_record', COUNT(*) FROM payment_record
UNION ALL SELECT 'meal_activity', COUNT(*) FROM meal_activity
UNION ALL SELECT 'meal_activity_member', COUNT(*) FROM meal_activity_member
UNION ALL SELECT 'user_behavior', COUNT(*) FROM user_behavior
UNION ALL SELECT 'user_feed_post', COUNT(*) FROM user_feed_post;

-- 点赞数不一致（应返回空）
SELECT r.id, r.like_count, COUNT(l.id) AS real_cnt
FROM food_review r
LEFT JOIN review_like l ON l.review_id = r.id
GROUP BY r.id, r.like_count
HAVING r.like_count <> real_cnt;

-- 订单金额不一致（应返回空）
SELECT o.id, o.total_amount, SUM(i.amount) AS item_sum
FROM food_order o
JOIN order_item i ON i.order_id = o.id
GROUP BY o.id, o.total_amount
HAVING o.total_amount <> item_sum;

-- 实付金额不一致（应返回空）
SELECT id FROM food_order WHERE pay_amount <> total_amount - discount_amount;

-- 拼单人数不一致（应返回空）
SELECT a.id, a.current_members, COUNT(m.id) AS real_cnt
FROM meal_activity a
LEFT JOIN meal_activity_member m ON m.activity_id = a.id AND m.status = 1
GROUP BY a.id, a.current_members
HAVING a.current_members <> real_cnt;

-- 登录账号
SELECT id, nickname, phone, role_id, status FROM sys_user ORDER BY id;
   ============================================================================= */
