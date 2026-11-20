package com.campus.food.campusfood.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.CampaignSaveDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.MerchantCampaignService;
import com.campus.food.campusfood.vo.CampaignVO;

/**
 * 店主营销活动维护：创建、查询本店活动与效果数据、修改、下线。均需 JWT；创建须店铺已审核通过。
 *
 * @author yang
 * @since 2026-11-20
 */
@RestController
@RequestMapping("/merchant/campaigns")
public class MerchantCampaignController {

    @Autowired
    private MerchantCampaignService merchantCampaignService;

    /**
     * 创建活动，挂到当前用户第一家审核通过的店，直接发布为进行中。
     *
     * @param dto 活动标题、类型、规则与起止时间
     * @return 新建的活动
     * @throws BizException 未登录、没有店铺、店铺未审核通过或字段不合法时
     */
    @PostMapping
    public CampaignVO create(@RequestBody CampaignSaveDTO dto) {
        return merchantCampaignService.create(dto);
    }

    /**
     * 查询当前用户各店铺的活动，按创建时间倒序，附带参与人数、订单数、销售额合计。
     *
     * @return 活动列表；没有店铺或没有活动时为空数组
     * @throws BizException 未登录时
     */
    @GetMapping
    public List<CampaignVO> list() {
        return merchantCampaignService.listMine();
    }

    /**
     * 修改本人店铺的活动。已结束、已下线的活动不可修改。
     *
     * @param id  活动 ID，不能为空
     * @param dto 待写入的标题、类型、规则与起止时间
     * @return 修改后的活动
     * @throws BizException 未登录、活动不存在、无权操作、活动已结束/下线或字段不合法时
     */
    @PutMapping("/{id}")
    public CampaignVO update(@PathVariable Long id, @RequestBody CampaignSaveDTO dto) {
        return merchantCampaignService.update(id, dto);
    }

    /**
     * 下线本人店铺的活动，状态置为 3；已下线的活动重复调用不报错。
     *
     * @param id 活动 ID，不能为空
     * @throws BizException 未登录、活动不存在、无权操作或活动已结束时
     */
    @PostMapping("/{id}/offline")
    public void offline(@PathVariable Long id) {
        merchantCampaignService.offline(id);
    }
}