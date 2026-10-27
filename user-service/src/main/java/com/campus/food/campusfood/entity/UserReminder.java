package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户提醒开关，对应表 {@code user_reminder}。
 *
 * @author yang
 * @since 2026-10-27
 */
@Data
public class UserReminder {

    private Long id;

    private Long userId;

    /** CAMPAIGN / FRIEND_FEED / ORDER */
    private String remindType;

    /** 本期仅 INBOX */
    private String channel;

    /** 0 关 / 1 开 */
    private Integer enabled;

    private String refType;

    private Long refId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
