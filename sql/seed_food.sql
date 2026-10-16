-- 演示数据：分类 + 店铺 + 上架菜品（owner_user_id 指向已存在用户，默认 1）
-- 在 MySQL 中执行：SOURCE sql/seed_food.sql;  或在客户端打开本文件运行。

INSERT IGNORE INTO food_category (id, parent_id, name, sort_no, status) VALUES
(1, 0, '中餐', 1, 1),
(2, 0, '西餐', 2, 1),
(3, 0, '小吃', 3, 1),
(4, 1, '盖浇饭', 1, 1);

INSERT INTO merchant (id, owner_user_id, shop_name, campus_area, address, contact_phone, audit_status, status, deleted)
VALUES (1, 1, '一食堂窗口A', '东区食堂', '东区一食堂一层', '13800000000', 1, 1, 0)
ON DUPLICATE KEY UPDATE
  shop_name = VALUES(shop_name),
  campus_area = VALUES(campus_area),
  audit_status = VALUES(audit_status),
  status = VALUES(status),
  deleted = 0;

INSERT INTO food_item (
  id, merchant_id, category_id, name, food_type, price, stock, description,
  avg_score, review_count, sales_count, recommend_tag, status, deleted
) VALUES
(1, 1, 1, '番茄炒蛋盖饭', '中餐', 12.00, 100, '家常口味，米饭浇汁。', 4.50, 12, 88, '招牌', 1, 0),
(2, 1, 4, '红烧肉盖饭', '中餐', 16.00, 80, '肥瘦相间，配时蔬。', 4.70, 20, 120, '热门', 1, 0),
(3, 1, 3, '手抓饼', '小吃', 8.00, 50, '现烙外酥里软。', 4.20, 6, 40, '新品', 1, 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  price = VALUES(price),
  status = VALUES(status),
  deleted = 0;

-- 演示评价（user_id=1 需已存在；审核通过可直接在列表看到）
INSERT INTO food_review (id, user_id, food_id, merchant_id, score, content, like_count, audit_status, status)
VALUES
(1, 1, 1, 1, 5, '番茄很入味，推荐。', 0, 1, 1),
(2, 1, 1, 1, 4, '份量够，就是有点咸。', 0, 1, 1)
ON DUPLICATE KEY UPDATE
  score = VALUES(score),
  content = VALUES(content),
  audit_status = VALUES(audit_status),
  status = VALUES(status);

-- 演示评价（user_id=1，需库中已有该用户；audit_status=1 可见）
INSERT INTO food_review (id, user_id, food_id, merchant_id, score, content, like_count, audit_status, status)
VALUES
(1, 1, 1, 1, 5, '番茄很入味，米饭份量足。', 2, 1, 1),
(2, 1, 1, 1, 4, '不错，略咸一点点。', 0, 1, 1)
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  score = VALUES(score),
  audit_status = VALUES(audit_status),
  status = VALUES(status);
