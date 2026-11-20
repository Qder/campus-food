package com.campus.food.campusfood.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.entity.MarketingCampaign;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.mapper.MarketingCampaignMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.service.FoodCampaignService;
import com.campus.food.campusfood.vo.CampaignVO;

/**
 * 用户端营销活动查询实现。只返回进行中且在时间窗内的活动；店铺名按 merchantId 回表补齐。
 *
 * @author yang
 * @since 2026-11-20
 */
@Service
public class FoodCampaignServiceImpl implements FoodCampaignService {

    @Autowired
    private MarketingCampaignMapper marketingCampaignMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /** {@inheritDoc} */
    @Override
    public List<CampaignVO> listActive(Long merchantId) {
        LocalDateTime now = LocalDateTime.now();
        marketingCampaignMapper.expireOverdue(now);
        List<MarketingCampaign> campaigns = marketingCampaignMapper.selectActive(merchantId, now);
        if (campaigns.isEmpty()) {
            return List.of();
        }

        Map<Long, Merchant> shopMap = new HashMap<>();
        List<CampaignVO> list = new ArrayList<>();
        for (MarketingCampaign campaign : campaigns) {
            Merchant shop = shopMap.get(campaign.getMerchantId());
            if (shop == null && !shopMap.containsKey(campaign.getMerchantId())) {
                shop = merchantMapper.selectById(campaign.getMerchantId());
                shopMap.put(campaign.getMerchantId(), shop);
            }
            list.add(toVo(campaign, shop));
        }
        return list;
    }

    /** 实体转 VO，店铺名为 null 时不填；用户端不返回效果统计。 */
    private CampaignVO toVo(MarketingCampaign campaign, Merchant shop) {
        CampaignVO vo = new CampaignVO();
        vo.setId(campaign.getId());
        vo.setMerchantId(campaign.getMerchantId());
        if (shop != null) {
            vo.setShopName(shop.getShopName());
        }
        vo.setTitle(campaign.getTitle());
        vo.setCampaignType(campaign.getCampaignType());
        vo.setRuleJson(campaign.getRuleJson());
        vo.setStartAt(campaign.getStartAt());
        vo.setEndAt(campaign.getEndAt());
        vo.setStatus(campaign.getStatus());
        vo.setCreatedAt(campaign.getCreatedAt());
        vo.setUpdatedAt(campaign.getUpdatedAt());
        return vo;
    }
}