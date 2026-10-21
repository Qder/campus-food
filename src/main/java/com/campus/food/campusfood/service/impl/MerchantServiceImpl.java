package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 店铺业务实现。入驻申请绑定当前用户；改删前校验店主身份。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    /** 经营状态：营业。用户端只展示审核通过且营业中的店 */
    private static final int STATUS_OPEN = 1;

    @Autowired
    private MerchantMapper merchantMapper;

    /** {@inheritDoc} */
    @Override
    public int insert(Merchant merchant) {
        if (merchant == null) {
            throw new BizException("店铺信息不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);
        merchant.setOwnerUserId(requireCurrentUserId());
        if (merchant.getAuditStatus() == null) {
            merchant.setAuditStatus(MerchantAuditStatus.PENDING.getCode());
        }
        if (merchant.getStatus() == null) {
            merchant.setStatus(0);
        }
        if (merchant.getDeleted() == null) {
            merchant.setDeleted(0);
        }
        int rows = merchantMapper.insert(merchant);
        if (rows <= 0) {
            throw new BizException("入驻申请失败");
        }
        return rows;
    }

    /** {@inheritDoc} */
    @Override
    public int deleteById(Long id) {
        if (id == null) {
            throw new BizException("店铺 ID 不能为空");
        }
        requireOwnedMerchant(id);
        int rows = merchantMapper.deleteById(id);
        if (rows <= 0) {
            throw new BizException("删除失败");
        }
        return rows;
    }

    /** {@inheritDoc} */
    @Override
    public List<Merchant> selectByOwnerUserId(Long ownerUserId) {
        if (ownerUserId == null) {
            return Collections.emptyList();
        }
        List<Merchant> list = merchantMapper.selectByOwnerUserId(ownerUserId);
        return list != null ? list : Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Merchant> selectByCondition(MerchantQueryDTO merchantQueryDTO) {
        MerchantQueryDTO query = merchantQueryDTO != null ? merchantQueryDTO : new MerchantQueryDTO();
        List<Merchant> list = merchantMapper.selectByCondition(query);
        return list != null ? list : Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public Merchant selectById(Long id) {
        if (id == null) {
            return null;
        }
        return merchantMapper.selectById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Merchant> selectAll() {
        List<Merchant> list = merchantMapper.selectAll();
        return list != null ? list : Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public int update(Merchant merchant) {
        if (merchant == null || merchant.getId() == null) {
            throw new BizException("店铺 ID 不能为空");
        }
        requireOwnedMerchant(merchant.getId());
        int rows = merchantMapper.update(merchant);
        if (rows <= 0) {
            throw new BizException("更新失败");
        }
        return rows;
    }

    /** {@inheritDoc} */
    @Override
    public Merchant getVisibleById(Long id) {
        if (id == null) {
            throw new BizException("店铺 ID 不能为空");
        }
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BizException("店铺不存在");
        }
        if (!Integer.valueOf(MerchantAuditStatus.APPROVED.getCode()).equals(merchant.getAuditStatus())
                || !Integer.valueOf(STATUS_OPEN).equals(merchant.getStatus())) {
            throw new BizException("店铺未营业或不可查看");
        }
        return merchant;
    }

    /** {@inheritDoc} */
    @Override
    public List<Merchant> listMine() {
        return selectByOwnerUserId(requireCurrentUserId());
    }

    /** 当前登录用户。未登录时中断，避免入驻申请没有店主。 */
    private Long requireCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        return userId;
    }

    /** 店铺必须存在且店主是当前用户，改删不能碰到别人的店。 */
    private void requireOwnedMerchant(Long merchantId) {
        Long currentUserId = requireCurrentUserId();
        Merchant existing = merchantMapper.selectById(merchantId);
        if (existing == null) {
            throw new BizException("店铺不存在");
        }
        if (!currentUserId.equals(existing.getOwnerUserId())) {
            throw new BizException("无权操作该店铺");
        }
    }
}
