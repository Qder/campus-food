package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.vo.CampaignVO;

/**
 * 用户端营销活动查询：只展示进行中且处于起止时间窗内的活动，无需登录。
 *
 * @author yang
 * @since 2026-11-20
 */
public interface FoodCampaignService {

    /**
     * 查询进行中的活动，按创建时间倒序，最多 100 条。
     *
     * @param merchantId 店铺 ID，可选，null 表示全部店铺
     * @return 活动列表，带店铺名；无数据时为空列表
     */
    List<CampaignVO> listActive(Long merchantId);
}