package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 营销活动，对应表 {@code marketing_campaign}。
 *
 * @author yang
 * @since 2026-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketingCampaign {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动类型:DISCOUNT/FULL_REDUCE/GROUP_BUY
     */
    private String campaignType;

    /**
     * 规则JSON(折扣/满减门槛等)
     */
    private String ruleJson;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    /**
     * 状态:0草稿1进行中2结束3下线
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}