package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 提醒开关项，对应 V1.3 {@code GET /user/reminders} 列表元素。
 *
 * @author yang
 * @since 2026-10-27
 */
@Data
public class RemindVO {

    /** CAMPAIGN / FRIEND_FEED / ORDER */
    private String remindType;

    /** 展示名，如「优惠活动」；不入库 */
    private String label;

    /** 本期为 INBOX */
    private String channel;

    /** 是否向该渠道推送 */
    private Boolean enabled;
}
