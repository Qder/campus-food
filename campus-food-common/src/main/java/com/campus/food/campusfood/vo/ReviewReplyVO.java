package com.campus.food.campusfood.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复视图对象，用于发表回复与回复列表。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
public class ReviewReplyVO {

    /** 回复 ID */
    private Long id;

    /** 评价 ID */
    private Long reviewId;

    /** 回复人用户 ID */
    private Long userId;

    /** 回复人昵称 */
    private String nickname;

    /** 回复内容 */
    private String content;

    /** 是否商家回复：0 否，1 是 */
    private Integer fromMerchant;

    /** 回复时间 */
    private LocalDateTime createdAt;
}