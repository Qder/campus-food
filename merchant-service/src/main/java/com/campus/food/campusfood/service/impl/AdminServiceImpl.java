package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.MerchantCreateDTO;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 管理员店铺审核实现。通过或代建时把店主角色升为商家。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class AdminServiceImpl implements AdminService {

    /** 商家角色，对应 {@code sys_role} 种子 MERCHANT */
    private static final int ROLE_MERCHANT = 2;
    /** 经营状态：营业 */
    private static final int STATUS_OPEN = 1;
    /** 未逻辑删除 */
    private static final int NOT_DELETED = 0;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    /** {@inheritDoc} */
    @Override
    public List<Merchant> getPendingMerchants() {
        MerchantQueryDTO queryDTO = new MerchantQueryDTO();
        queryDTO.setAuditStatus(MerchantAuditStatus.PENDING.getCode());
        List<Merchant> list = merchantMapper.selectByCondition(queryDTO);
        return list != null ? list : Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>管理员代建的店铺直接记为审核通过并开业，不再进入待审。
     */
    @Override
    public Merchant createMerchant(MerchantCreateDTO merchantCreateDTO) {
        if (merchantCreateDTO == null) {
            throw new BizException("店铺信息不能为空");
        }
        UserInfo owner = userFeignClient.getUserInfo(merchantCreateDTO.getOwnerUserId());
        if (owner == null) {
            throw new BizException("店主用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        Long adminId = requireAdminUserId();

        Merchant merchant = toMerchant(merchantCreateDTO);
        merchant.setAuditStatus(MerchantAuditStatus.APPROVED.getCode());
        merchant.setAuditedAt(now);
        merchant.setAuditedBy(adminId);
        merchant.setSettledAt(now);
        merchant.setStatus(STATUS_OPEN);
        merchant.setDeleted(NOT_DELETED);
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);

        int rows = merchantMapper.insert(merchant);
        if (rows <= 0) {
            throw new BizException("创建店铺失败");
        }
        grantMerchantRole(merchant.getOwnerUserId());
        return merchantMapper.selectById(merchant.getId());
    }

    /**
     * {@inheritDoc}
     *
     * <p>只有待审店铺能通过；通过后开业并授予店主商家角色。
     */
    @Override
    public Merchant approveMerchant(Long id, String auditRemark) {
        Merchant existing = requirePendingMerchant(id);
        LocalDateTime now = LocalDateTime.now();
        Long adminId = requireAdminUserId();

        Merchant patch = new Merchant();
        patch.setId(id);
        patch.setAuditStatus(MerchantAuditStatus.APPROVED.getCode());
        patch.setAuditedAt(now);
        patch.setAuditedBy(adminId);
        patch.setSettledAt(now);
        patch.setStatus(STATUS_OPEN);
        if (StringUtils.hasText(auditRemark)) {
            patch.setAuditRemark(auditRemark.trim());
        }

        int rows = merchantMapper.update(patch);
        if (rows <= 0) {
            throw new BizException("审核通过失败");
        }
        grantMerchantRole(existing.getOwnerUserId());
        return merchantMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>驳回原因写入审核备注，经营状态保持不变。
     */
    @Override
    public Merchant rejectMerchant(Long id, MerchantRejectDTO merchantRejectDTO) {
        requirePendingMerchant(id);
        if (merchantRejectDTO == null || !StringUtils.hasText(merchantRejectDTO.getReason())) {
            throw new BizException("拒绝原因不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Long adminId = requireAdminUserId();

        Merchant patch = new Merchant();
        patch.setId(id);
        patch.setAuditStatus(MerchantAuditStatus.REJECTED.getCode());
        patch.setAuditRemark(merchantRejectDTO.getReason().trim());
        patch.setAuditedAt(now);
        patch.setAuditedBy(adminId);

        int rows = merchantMapper.update(patch);
        if (rows <= 0) {
            throw new BizException("驳回失败");
        }
        return merchantMapper.selectById(id);
    }

    /** 店铺必须存在且仍为待审，否则通过或驳回没有意义。 */
    private Merchant requirePendingMerchant(Long id) {
        if (id == null) {
            throw new BizException("店铺 ID 不能为空");
        }
        Merchant existing = merchantMapper.selectById(id);
        if (existing == null) {
            throw new BizException("店铺不存在");
        }
        if (!Integer.valueOf(MerchantAuditStatus.PENDING.getCode()).equals(existing.getAuditStatus())) {
            throw new BizException("仅待审核店铺可操作");
        }
        return existing;
    }

    /** 当前登录管理员，写入 auditedBy。 */
    private Long requireAdminUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        return userId;
    }

    private static Merchant toMerchant(MerchantCreateDTO dto) {
        Merchant merchant = new Merchant();
        merchant.setOwnerUserId(dto.getOwnerUserId());
        merchant.setShopName(dto.getShopName());
        merchant.setCampusArea(dto.getCampusArea());
        merchant.setAddress(dto.getAddress());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setLogoUrl(dto.getLogoUrl());
        merchant.setDescription(dto.getDescription());
        merchant.setLicenseImgUrl(dto.getLicenseImgUrl());
        return merchant;
    }

    /** 审核通过后把店主升为商家；已经是商家则不再改角色。 */
    private void grantMerchantRole(Long ownerUserId) {
        if (ownerUserId == null) {
            return;
        }
        UserInfo owner = userFeignClient.getUserInfo(ownerUserId);
        if (owner == null) {
            throw new BizException("店主用户不存在");
        }
        if (Integer.valueOf(ROLE_MERCHANT).equals(owner.getRoleId())) {
            return;
        }
        userFeignClient.setMerchantRole(ownerUserId);
    }
}
