package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 点赞切换结果，对应 {@code POST /food/reviews/{id}/like}。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
public class ReviewLikeVO {

    /** 评价 ID */
    private Long reviewId;

    /** 本次操作后是否处于已点赞状态 */
    private Boolean liked;

    /** 操作后的点赞总数 */
    private Integer likeCount;
}