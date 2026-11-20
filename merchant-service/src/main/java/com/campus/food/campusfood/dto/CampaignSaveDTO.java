package com.campus.food.campusfood.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 店主创建或修改营销活动的请求体。
 *
 * @author yang
 * @since 2026-11-20
 */
@Data
public class CampaignSaveDTO {

    /**
     * 活动标题，去空白后 1–128 字
     */
    private String title;

    /**
     * 活动类型：DISCOUNT / FULL_REDUCE / GROUP_BUY
     */
    private String campaignType;

    /**
     * 规则 JSON 字符串，如 {"full":30,"reduce":5}
     */
    private String ruleJson;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间，须晚于开始时间与当前时间
     */
    private LocalDateTime endAt;

}