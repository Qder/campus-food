package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 拼单结束后的反馈，对应表 {@code meal_activity_feedback}。
 *
 * <p>{@code toUserId} 为空表示对活动整体评价，否则是评价某位参与者。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealActivityFeedback {

    private Long id;

    private Long activityId;

    private Long fromUserId;

    /** 被评价人；空表示对活动 */
    private Long toUserId;

    /** 评分 1–5 */
    private Integer score;

    private String content;

    private LocalDateTime createdAt;
}
