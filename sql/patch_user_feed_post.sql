-- ============================================================
-- campus-food/sql/patch_user_feed_post.sql
-- 用户美食动态（Feed 正文持久化）
-- 适用数据库：campus_food
-- ============================================================

USE campus_food;

DROP TABLE IF EXISTS user_feed_post;

CREATE TABLE user_feed_post (
                                id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态ID',
                                user_id     BIGINT UNSIGNED NOT NULL COMMENT '发布用户ID',
                                content     VARCHAR(2000) NOT NULL COMMENT '动态文字内容',
                                food_id     BIGINT UNSIGNED DEFAULT NULL COMMENT '关联美食ID，可空',
                                image_urls  VARCHAR(2000) DEFAULT NULL COMMENT '图片URL，多个用英文逗号分隔，可空',
                                created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                                PRIMARY KEY (id),
                                KEY idx_feed_user_time (user_id, created_at),
                                KEY idx_feed_food (food_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='用户美食动态(Feed)';