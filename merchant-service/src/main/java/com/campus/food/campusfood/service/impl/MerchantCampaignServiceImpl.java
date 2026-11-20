package com.campus.food.campusfood.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CampaignSaveDTO;
import com.campus.food.campusfood.entity.CampaignStat;
import com.campus.food.campusfood.entity.MarketingCampaign;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.enums.CampaignStatus;
import com.campus.food.campusfood.enums.CampaignType;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.CampaignStatMapper;
import com.campus.food.campusfood.mapper.MarketingCampaignMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.service.MerchantCampaignService;
import com.campus.food.campusfood.vo.CampaignVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 店主营销活动维护实现。创建只挂到当前用户第一家审核通过的店；修改与下线须通过
 * {@link #requireOwnedCampaign(Long)} 防越权；列表附带 campaign_stat 汇总的效果数据。
 *
 * @author yang
 * @since 2026-11-20
 */
@Service
public class MerchantCampaignServiceImpl implements MerchantCampaignService {

    /** 活动标题长度上限，与 marketing_campaign.title 列一致 */
    private static final int TITLE_MAX_LENGTH = 128;

    /** 规则 JSON 校验用，readTree 线程安全 */
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private MarketingCampaignMapper marketingCampaignMapper;

    @Autowired
    private CampaignStatMapper campaignStatMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /** {@inheritDoc} */
    @Override
    public CampaignVO create(CampaignSaveDTO dto) {
        Merchant shop = requireApprovedShop();
        LocalDateTime now = LocalDateTime.now();
        validate(dto, now);

        MarketingCampaign campaign = new MarketingCampaign();
        campaign.setMerchantId(shop.getId());
        applyDto(campaign, dto);
        campaign.setStatus(CampaignStatus.ACTIVE.getCode());
        campaign.setCreatedAt(now);
        campaign.setUpdatedAt(now);
        marketingCampaignMapper.insert(campaign);

        CampaignVO vo = toVo(campaign, shop);
        fillStats(vo, null);
        return vo;
    }

    /** {@inheritDoc} */
    @Override
    public List<CampaignVO> listMine() {
        Long curUserId = requireUserId();
        List<Merchant> shops = merchantMapper.selectByOwnerUserId(curUserId);
        if (shops == null || shops.isEmpty()) {
            return List.of();
        }
        Map<Long, Merchant> shopMap = new HashMap<>();
        List<Long> merchantIds = new ArrayList<>();
        for (Merchant shop : shops) {
            shopMap.put(shop.getId(), shop);
            merchantIds.add(shop.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        marketingCampaignMapper.expireOverdue(now);
        List<MarketingCampaign> campaigns = marketingCampaignMapper.selectByMerchantIds(merchantIds);
        if (campaigns.isEmpty()) {
            return List.of();
        }

        List<Long> campaignIds = new ArrayList<>();
        for (MarketingCampaign campaign : campaigns) {
            campaignIds.add(campaign.getId());
        }
        Map<Long, CampaignStat> statMap = new HashMap<>();
        for (CampaignStat stat : campaignStatMapper.selectTotalsByCampaignIds(campaignIds)) {
            statMap.put(stat.getCampaignId(), stat);
        }

        List<CampaignVO> list = new ArrayList<>();
        for (MarketingCampaign campaign : campaigns) {
            CampaignVO vo = toVo(campaign, shopMap.get(campaign.getMerchantId()));
            fillStats(vo, statMap.get(campaign.getId()));
            list.add(vo);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public CampaignVO update(Long id, CampaignSaveDTO dto) {
        MarketingCampaign campaign = requireOwnedCampaign(id);
        if (CampaignStatus.ENDED.getCode() == campaign.getStatus()) {
            throw new BizException("活动已结束，不能修改");
        }
        if (CampaignStatus.OFFLINE.getCode() == campaign.getStatus()) {
            throw new BizException("活动已下线，不能修改");
        }
        LocalDateTime now = LocalDateTime.now();
        validate(dto, now);

        applyDto(campaign, dto);
        if (campaign.getEndAt().isBefore(now)) {
            campaign.setStatus(CampaignStatus.ENDED.getCode());
        }
        campaign.setUpdatedAt(now);
        marketingCampaignMapper.updateById(campaign);

        Merchant shop = merchantMapper.selectById(campaign.getMerchantId());
        CampaignVO vo = toVo(campaign, shop);
        List<CampaignStat> stats = campaignStatMapper.selectTotalsByCampaignIds(List.of(id));
        fillStats(vo, stats.isEmpty() ? null : stats.get(0));
        return vo;
    }

    /** {@inheritDoc} */
    @Override
    public void offline(Long id) {
        MarketingCampaign campaign = requireOwnedCampaign(id);
        if (CampaignStatus.ENDED.getCode() == campaign.getStatus()) {
            throw new BizException("活动已结束，无需下线");
        }
        if (CampaignStatus.OFFLINE.getCode() == campaign.getStatus()) {
            return;
        }
        marketingCampaignMapper.updateStatusById(id, CampaignStatus.OFFLINE.getCode());
    }

    /** 校验标题、类型、规则 JSON 与起止时间，任一不合法即中断。 */
    private void validate(CampaignSaveDTO dto, LocalDateTime now) {
        if (dto == null) {
            throw new BizException("请求体不能为空");
        }
        String title = dto.getTitle() == null ? null : dto.getTitle().trim();
        if (title == null || title.isEmpty()) {
            throw new BizException("活动标题不能为空");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new BizException("活动标题过长");
        }
        if (CampaignType.of(dto.getCampaignType()) == null) {
            throw new BizException("活动类型不合法");
        }
        String ruleJson = dto.getRuleJson() == null ? null : dto.getRuleJson().trim();
        if (ruleJson == null || ruleJson.isEmpty()) {
            throw new BizException("活动规则不能为空");
        }
        try {
            JsonNode node = JSON.readTree(ruleJson);
            if (!node.isObject()) {
                throw new BizException("活动规则须为 JSON 对象");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("活动规则不是合法 JSON");
        }
        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            throw new BizException("起止时间不能为空");
        }
        if (!dto.getEndAt().isAfter(dto.getStartAt())) {
            throw new BizException("结束时间须晚于开始时间");
        }
        if (!dto.getEndAt().isAfter(now)) {
            throw new BizException("结束时间须晚于当前时间");
        }
    }

    /** 把请求体字段写入实体，标题与规则去首尾空白。 */
    private void applyDto(MarketingCampaign campaign, CampaignSaveDTO dto) {
        campaign.setTitle(dto.getTitle().trim());
        campaign.setCampaignType(dto.getCampaignType());
        campaign.setRuleJson(dto.getRuleJson().trim());
        campaign.setStartAt(dto.getStartAt());
        campaign.setEndAt(dto.getEndAt());
    }

    /** 当前用户第一家审核通过的店，供创建绑 merchantId。 */
    private Merchant requireApprovedShop() {
        Long curUserId = requireUserId();
        List<Merchant> merchants = merchantMapper.selectByOwnerUserId(curUserId);
        if (merchants == null || merchants.isEmpty()) {
            throw new BizException("您没有店铺");
        }
        for (Merchant merchant : merchants) {
            if (Integer.valueOf(MerchantAuditStatus.APPROVED.getCode()).equals(merchant.getAuditStatus())) {
                return merchant;
            }
        }
        throw new BizException("您的店铺未审核通过");
    }

    /** 活动存在且属于当前用户的店。 */
    private MarketingCampaign requireOwnedCampaign(Long id) {
        if (id == null) {
            throw new BizException("活动 ID 不能为空");
        }
        Long curUserId = requireUserId();
        MarketingCampaign campaign = marketingCampaignMapper.selectById(id);
        if (campaign == null) {
            throw new BizException("活动不存在");
        }
        Merchant shop = merchantMapper.selectById(campaign.getMerchantId());
        if (shop == null || !curUserId.equals(shop.getOwnerUserId())) {
            throw new BizException("您没有权限操作");
        }
        return campaign;
    }

    /** 取当前登录用户。未登录时中断。 */
    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        return userId;
    }

    /** 实体转 VO，店铺名为 null 时不填。 */
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

    /** 填效果统计，无统计行时按 0 展示。 */
    private void fillStats(CampaignVO vo, CampaignStat stat) {
        if (stat == null) {
            vo.setParticipantCnt(0);
            vo.setOrderCnt(0);
            vo.setSalesAmount(BigDecimal.ZERO);
            return;
        }
        vo.setParticipantCnt(stat.getParticipantCnt());
        vo.setOrderCnt(stat.getOrderCnt());
        vo.setSalesAmount(stat.getSalesAmount());
    }
}