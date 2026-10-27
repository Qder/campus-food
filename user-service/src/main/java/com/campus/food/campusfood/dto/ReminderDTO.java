package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 修改提醒开关的入参，对应 V1.3 {@code PUT /user/reminders}。
 *
 * @author yang
 * @since 2026-10-27
 */
@Data
public class ReminderDTO {

    /** CAMPAIGN / FRIEND_FEED / ORDER */
    private String remindType;

    /** 是否开启 */
    private Boolean enabled;

    /** 可选，本期仅 INBOX */
    private String channel;
}
