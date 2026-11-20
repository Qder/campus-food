package com.campus.food.campusfood.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.MarketingCampaign;

/**
 * 营销活动持久层，对应表 {@code marketing_campaign}。
 *
 * @author yang
 * @since 2026-11-20
 */
public interface MarketingCampaignMapper {

    /**
     * 新增活动，主键回写到 {@code campaign.id}。
     *
     * @param campaign 活动实体，商家、标题、类型、规则、起止时间、状态不能为空
     * @return 受影响行数
     */
    int insert(MarketingCampaign campaign);

    /**
     * 按主键查询活动。
     *
     * @param id 活动 ID
     * @return 活动；不存在返回 null
     */
    MarketingCampaign selectById(@Param("id") Long id);

    /**
     * 按店铺 ID 集合查询活动，按创建时间倒序，最多 100 条。
     *
     * @param merchantIds 店铺 ID 集合，不能为空
     * @return 活动列表，无数据时为空列表
     */
    List<MarketingCampaign> selectByMerchantIds(@Param("merchantIds") List<Long> merchantIds);

    /**
     * 查询进行中且处于起止时间窗内的活动，按创建时间倒序，最多 100 条。
     *
     * @param merchantId 店铺 ID，null 表示不限店铺
     * @param now        当前时间
     * @return 活动列表，无数据时为空列表
     */
    List<MarketingCampaign> selectActive(@Param("merchantId") Long merchantId,
                                         @Param("now") LocalDateTime now);

    /**
     * 按主键更新活动标题、类型、规则、起止时间与状态。
     *
     * @param campaign 含主键的待更新活动
     * @return 受影响行数
     */
    int updateById(MarketingCampaign campaign);

    /**
     * 按主键只更新状态。
     *
     * @param id     活动 ID
     * @param status 新状态码
     * @return 受影响行数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 把已过结束时间的进行中活动批量置为已结束。
     *
     * @param now 当前时间
     * @return 受影响行数
     */
    int expireOverdue(@Param("now") LocalDateTime now);
}