package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private static final int ROLE_MERCHANT = 2;
    private static final int STATUS_OPEN = 1;
    private static final int NOT_DELETED = 0;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Merchant> getPendingMerchants() {
        MerchantQueryDTO queryDTO = new MerchantQueryDTO();
        queryDTO.setAuditStatus(MerchantAuditStatus.PENDING.getCode());
        List<Merchant> list = merchantMapper.selectByCondition(queryDTO);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public Merchant createMerchant(Merchant merchant) {
        if (merchant == null) {
            throw new BizException("店铺信息不能为空");
        }
        if (merchant.getOwnerUserId() == null) {
            throw new BizException("店主用户 ID 不能为空");
        }
        if (!StringUtils.hasText(merchant.getShopName())) {
            throw new BizException("店铺名称不能为空");
        }
        User owner = userMapper.selectById(merchant.getOwnerUserId());
        if (owner == null) {
            throw new BizException("店主用户不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        Long adminId = requireAdminUserId();
        merchant.setAuditStatus(MerchantAuditStatus.APPROVED.getCode());
        merchant.setAuditedAt(now);
        merchant.setAuditedBy(adminId);
        merchant.setSettledAt(now);
        if (merchant.getStatus() == null) {
            merchant.setStatus(STATUS_OPEN);
        }
        if (merchant.getDeleted() == null) {
            merchant.setDeleted(NOT_DELETED);
        }
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);

        int rows = merchantMapper.insert(merchant);
        if (rows <= 0) {
            throw new BizException("创建店铺失败");
        }
        grantMerchantRole(merchant.getOwnerUserId());
        return merchantMapper.selectById(merchant.getId());
    }

    @Override
    public Merchant updateMerchant(Long id, Merchant merchant) {
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
        if (merchant != null && StringUtils.hasText(merchant.getAuditRemark())) {
            patch.setAuditRemark(merchant.getAuditRemark().trim());
        }

        int rows = merchantMapper.update(patch);
        if (rows <= 0) {
            throw new BizException("审核通过失败");
        }
        grantMerchantRole(existing.getOwnerUserId());
        return merchantMapper.selectById(id);
    }

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

    private Long requireAdminUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        return userId;
    }

    private void grantMerchantRole(Long ownerUserId) {
        if (ownerUserId == null) {
            return;
        }
        User owner = userMapper.selectById(ownerUserId);
        if (owner == null) {
            return;
        }
        if (Integer.valueOf(ROLE_MERCHANT).equals(owner.getRoleId())) {
            return;
        }
        User patch = new User();
        patch.setId(ownerUserId);
        patch.setRoleId(ROLE_MERCHANT);
        userMapper.updateById(patch);
    }
}
