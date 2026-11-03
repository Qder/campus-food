package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 活动反馈入参，对应 {@code POST /social/meal-activities/{id}/feedback}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class SubmitMealFeedbackDTO {

    /** 被评价人；不传表示对活动整体 */
    private Long toUserId;

    /** 评分 1–5 */
    private Integer score;

    /** 文字，可空，≤ 500 字 */
    private String content;
}
