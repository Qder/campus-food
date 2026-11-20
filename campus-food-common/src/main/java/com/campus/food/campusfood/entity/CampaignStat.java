package com.campus.food.campusfood.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 营销活动效果按日统计，对应表 {@code campaign_stat}。
 *
 * @author yang
 * @since 2026-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStat {

    /**
     * 主键
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long campaignId;

    /**
     * 参与人数
     */
    private Integer participantCnt;

    /**
     * 订单数
     */
    private Integer orderCnt;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 统计日
     */
    private LocalDate statDate;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}