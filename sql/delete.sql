USE campus_food;

-- 1. 删除 sys_user 表中多余或需要重置的字段
ALTER TABLE sys_user DROP COLUMN like_tags;
ALTER TABLE sys_user DROP COLUMN dislike_tags;
ALTER TABLE sys_user DROP COLUMN taste_tags;
ALTER TABLE sys_user DROP COLUMN dislike_ingredients;

-- 2. 删除 food_item 表中多余或需要重置的字段
ALTER TABLE food_item DROP COLUMN like_tags;
ALTER TABLE food_item DROP COLUMN ingredient_tags;
ALTER TABLE food_item DROP COLUMN taste_tags;
ALTER TABLE food_item DROP COLUMN ingredients;