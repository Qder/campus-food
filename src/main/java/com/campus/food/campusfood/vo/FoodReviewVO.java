package com.campus.food.campusfood.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价视图对象，用于评价列表与发表评价后的回显。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class FoodReviewVO {

    /** 评价 ID */
    private Long id;

    /** 评价人用户 ID */
    private Long userId;

    /** 评价人昵称 */
    private String nickname;

    /** 被评价的美食 ID */
    private Long foodId;

    /** 评分 1–5 */
    private Integer score;

    /** 评价内容，可为空 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
