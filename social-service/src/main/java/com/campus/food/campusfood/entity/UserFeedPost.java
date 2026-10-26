package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户美食动态（Feed 正文），对应表 {@code user_feed_post}。
 *
 * <p>只持久化动态本身，时间线由 Redis ZSet 维护，两者通过动态 ID 关联。
 *
 * @author yang
 * @since 2026-10-23
 */
@Data
public class UserFeedPost {

    /** 动态 ID */
    private Long id;

    /** 发布用户 ID */
    private Long userId;

    /** 关联美食 ID，可为空 */
    private Long foodId;

    /** 动态文字内容 */
    private String content;

    /** 发布时间 */
    private LocalDateTime createdAt;

    /** 图片 URL，多个用英文逗号分隔，可为空 */
    private String imageUrls;
}
