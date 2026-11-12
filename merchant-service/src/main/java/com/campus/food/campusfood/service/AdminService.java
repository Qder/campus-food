package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.MerchantCreateDTO;
import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.dto.UserListQueryDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.UserListVO;

/**
 * 平台管理员：店铺审核与用户列表。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface AdminService {

    /**
     * 查询待审核店铺。
     *
     * @return 待审店铺列表，无数据时为空列表
     */
    List<Merchant> getPendingMerchants();

    /**
     * 代建店铺。创建后直接审核通过并开业，同时把店主角色升为商家。
     *
     * @param merchantCreateDTO 店铺与店主信息，不能为空
     * @return 落库后的店铺
     * @throws com.campus.food.campusfood.exception.BizException 店主不存在或写入失败时
     */
    Merchant createMerchant(MerchantCreateDTO merchantCreateDTO);

    /**
     * 通过待审店铺。通过后开业，并授予店主商家角色。
     *
     * @param id          店铺 ID，不能为空
     * @param auditRemark 审核备注，可为 null 或空白
     * @return 更新后的店铺
     * @throws com.campus.food.campusfood.exception.BizException 未登录、店铺不存在或不是待审时
     */
    Merchant approveMerchant(Long id, String auditRemark);

    /**
     * 驳回待审店铺。原因写入审核备注，不改变经营状态。
     *
     * @param id                店铺 ID，不能为空
     * @param merchantRejectDTO 驳回原因，不能为空
     * @return 更新后的店铺
     * @throws com.campus.food.campusfood.exception.BizException 店铺不是待审或原因为空时
     */
    Merchant rejectMerchant(Long id, MerchantRejectDTO merchantRejectDTO);

    /**
     * 管理端用户分页列表。
     *
     * @param queryDTO 查询条件，可为 null（按默认分页、不筛选）
     * @return 分页结果，无数据时 {@code list} 为空
     * @throws com.campus.food.campusfood.exception.BizException 状态不合法或用户服务不可用时
     */
    PageVO<UserListVO> getUserPageList(UserListQueryDTO queryDTO);
}
