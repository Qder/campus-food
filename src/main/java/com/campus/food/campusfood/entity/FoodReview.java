package com.campus.food.campusfood.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 美食评价，对应表 {@code food_review}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodReview {

    /** 评价 ID */
    private Long id;

    /** 评价人用户 ID */
    private Long userId;

    /** 被评价的美食 ID */
    private Long foodId;

    /** 商家 ID（冗余，便于按商家聚合） */
    private Long merchantId;

    /** 关联订单 ID，课设场景可为空 */
    private Long orderId;

    /** 评分 1–5 */
    private Integer score;

    /** 评价内容，可为空 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 审核：0 待审，1 通过，2 拒绝 */
    private Integer auditStatus;

    /** 1 展示，0 隐藏 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
