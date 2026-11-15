package com.campus.food.campusfood.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价回复，对应表 {@code review_reply}。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReply {

    /** 主键 */
    private Long id;

    /** 评价 ID */
    private Long reviewId;

    /** 回复人用户 ID */
    private Long userId;

    /** 被回复用户 ID，课设场景可为空 */
    private Long replyToUid;

    /** 回复内容，最长 1000 字 */
    private String content;

    /** 是否商家回复：0 否，1 是 */
    private Integer fromMerchant;

    /** 审核状态：0 待审，1 通过，2 拒绝 */
    private Integer auditStatus;

    /** 回复时间 */
    private LocalDateTime createdAt;
}