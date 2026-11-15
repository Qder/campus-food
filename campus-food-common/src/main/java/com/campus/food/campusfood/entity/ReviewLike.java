package com.campus.food.campusfood.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价点赞，对应表 {@code review_like}；同一用户对同一评价只能有一条记录。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike {

    /** 主键 */
    private Long id;

    /** 评价 ID */
    private Long reviewId;

    /** 点赞用户 ID */
    private Long userId;

    /** 点赞时间 */
    private LocalDateTime createdAt;
}