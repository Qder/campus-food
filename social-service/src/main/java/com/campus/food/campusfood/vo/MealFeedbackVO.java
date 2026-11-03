package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 一条活动反馈。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealFeedbackVO {

    private Long feedbackId;

    private Long fromUserId;

    private String fromNickname;

    private Long toUserId;

    private Integer score;

    private String content;

    private LocalDateTime createdAt;
}
