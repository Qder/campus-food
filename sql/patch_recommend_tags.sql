-- ============================================================
-- campus-food/sql/patch_recommend_tags.sql
-- 推荐标签补丁：用户口味/忌口 + 菜品口味/食材
-- 适用数据库：campus_food
-- 说明：本脚本仅添加指定字段，并在种子数据中填充标签以便演示
-- ============================================================

USE campus_food;

-- ------------------------------------------------------------
-- 1. 用户表：补充口味标签与不喜欢的食材
-- ------------------------------------------------------------
ALTER TABLE sys_user ADD COLUMN taste_tags TEXT COMMENT '口味标签（如：辣,酸,甜）';
ALTER TABLE sys_user ADD COLUMN dislike_ingredients TEXT COMMENT '不喜欢的食材（如：香菜,折耳根）';

-- ------------------------------------------------------------
-- 2. 菜品表：补充口味标签与食材
-- ------------------------------------------------------------
ALTER TABLE food_item ADD COLUMN taste_tags TEXT COMMENT '口味标签（如：辣,咸鲜）';
ALTER TABLE food_item ADD COLUMN ingredients TEXT COMMENT '食材（如：鸡肉,花生,辣椒）';

-- ------------------------------------------------------------
-- 3. 种子数据：用户标签示例
-- 根据前序截图，sys_user 表中存在 id 为 1、2 的数据
-- ------------------------------------------------------------
UPDATE sys_user SET
                    taste_tags = '辣,咸鲜',
                    dislike_ingredients = '香菜,折耳根'
WHERE id = 1;

UPDATE sys_user SET
                    taste_tags = '清淡,鲜',
                    dislike_ingredients = '肥肉,动物内脏'
WHERE id = 2;

-- ------------------------------------------------------------
-- 4. 种子数据：菜品标签示例
-- 根据前序截图，food_item 表中存在 id 为 1、2、3 的数据
-- ------------------------------------------------------------
-- 菜品1：番茄炒蛋盖饭
UPDATE food_item SET
                     taste_tags = '咸鲜,微甜',
                     ingredients = '番茄,鸡蛋,米饭'
WHERE id = 1;

-- 菜品2：红烧肉盖饭
UPDATE food_item SET
                     taste_tags = '咸甜,浓郁',
                     ingredients = '五花肉,米饭,青菜'
WHERE id = 2;

-- 菜品3：手抓饼
UPDATE food_item SET
                     taste_tags = '咸香,酥脆',
                     ingredients = '面粉,鸡蛋,生菜'
WHERE id = 3;

-- ============================================================
-- 补丁结束
-- ============================================================