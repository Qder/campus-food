package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.CampaignStat;

/**
 * 营销活动效果统计持久层，对应表 {@code campaign_stat}。
 *
 * @author yang
 * @since 2026-11-20
 */
public interface CampaignStatMapper {

    /**
     * 按活动 ID 集合汇总参与人数、订单数与销售额（跨统计日求和）。
     *
     * @param campaignIds 活动 ID 集合，不能为空
     * @return 每个活动一行，仅填 campaignId 与三项汇总值；无数据时为空列表
     */
    List<CampaignStat> selectTotalsByCampaignIds(@Param("campaignIds") List<Long> campaignIds);
}