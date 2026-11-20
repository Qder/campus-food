package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.CampaignSaveDTO;
import com.campus.food.campusfood.vo.CampaignVO;

/**
 * 店主营销活动维护：创建、查询本店活动与效果数据、修改、下线。均需登录。
 *
 * @author yang
 * @since 2026-11-20
 */
public interface MerchantCampaignService {

    /**
     * 创建活动，挂到当前用户第一家审核通过的店，直接发布为进行中。
     *
     * @param dto 活动标题、类型、规则与起止时间
     * @return 新建的活动
     * @throws com.campus.food.campusfood.exception.BizException 未登录、没有店铺、店铺未审核通过或字段不合法时
     */
    CampaignVO create(CampaignSaveDTO dto);

    /**
     * 查询当前用户各店铺的活动，按创建时间倒序，附带参与人数、订单数、销售额合计。
     * 已过结束时间的进行中活动先落库为已结束。
     *
     * @return 活动列表；没有店铺或没有活动时为空列表
     * @throws com.campus.food.campusfood.exception.BizException 未登录时
     */
    List<CampaignVO> listMine();

    /**
     * 修改本人店铺的活动。已结束、已下线的活动不可修改。
     *
     * @param id  活动 ID
     * @param dto 待写入的标题、类型、规则与起止时间
     * @return 修改后的活动
     * @throws com.campus.food.campusfood.exception.BizException 未登录、活动不存在、无权操作、活动已结束/下线或字段不合法时
     */
    CampaignVO update(Long id, CampaignSaveDTO dto);

    /**
     * 下线本人店铺的活动，状态置为已下线；已下线的活动重复调用不报错。
     *
     * @param id 活动 ID
     * @throws com.campus.food.campusfood.exception.BizException 未登录、活动不存在、无权操作或活动已结束时
     */
    void offline(Long id);
}