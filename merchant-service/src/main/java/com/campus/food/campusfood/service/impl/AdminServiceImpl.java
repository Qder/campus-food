package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.MerchantCreateDTO;
import com.campus.food.campusfood.dto.MerchantQueryDTO;
import com.campus.food.campusfood.dto.MerchantRejectDTO;
import com.campus.food.campusfood.dto.NoticeDTO;
import com.campus.food.campusfood.dto.UserListQueryDTO;
import com.campus.food.campusfood.dto.UserStatusUpdateDTO;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.entity.PlatformNotice;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.mapper.PlatformNoticeMapper;
import com.campus.food.campusfood.service.AdminService;
import com.campus.food.campusfood.vo.NoticeVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.UserListVO;
import com.campus.food.campusfood.vo.UserStatusUpdateVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理员店铺审核、用户与公告实现。通过或代建时把店主角色升为商家。
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
    /** 用户列表默认页码 */
    private static final int DEFAULT_PAGE = 1;
    /** 用户列表默认每页条数 */
    private static final int DEFAULT_SIZE = 10;
    /** 用户列表每页条数上限 */
    private static final int MAX_SIZE = 50;
    /** 公告状态：草稿 */
    private static final int NOTICE_DRAFT = 0;
    /** 公告状态：已发布 */
    private static final int NOTICE_PUBLISHED = 1;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private PlatformNoticeMapper platformNoticeMapper;

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
     * <p>
     * 管理员代建的店铺直接记为审核通过并开业，不再进入待审。
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
     * <p>
     * 只有待审店铺能通过；通过后开业并授予店主商家角色。
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
     * <p>
     * 驳回原因写入审核备注，经营状态保持不变。
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

    /**
     * {@inheritDoc}
     *
     * <p>用户数据在 user-service：校正查询条件后经 Feign 分页查询。
     */
    @Override
    public PageVO<UserListVO> getUserPageList(UserListQueryDTO queryDTO) {
        String keyword = queryDTO == null ? null : queryDTO.getKeyword();
        Integer status = queryDTO == null ? null : queryDTO.getStatus();
        Integer page = queryDTO == null ? null : queryDTO.getPage();
        Integer size = queryDTO == null ? null : queryDTO.getSize();

        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        if (status != null && status != 0 && status != 1) {
            throw new BizException("状态不合法");
        }
        int p = page == null || page < 1 ? DEFAULT_PAGE : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        PageVO<UserListVO> pageVO = userFeignClient.listUsers(keyword, status, p, s);
        if (pageVO == null) {
            throw new BizException("用户服务不可用");
        }
        return pageVO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 用户数据在 user-service：这里只做入口校验，落库与业务校验由内部接口完成。
     */
    @Override
    public void setUserStatus(Long id, UserStatusUpdateDTO dto) {
        if (id == null) {
            throw new BizException("用户 ID 不能为空");
        }
        if (dto == null || dto.getStatus() == null) {
            throw new BizException("状态不能为空");
        }
        int status = dto.getStatus();
        if (status != 0 && status != 1) {
            throw new BizException("状态不合法：0 封禁，1 恢复");
        }
        String reason = dto.getReason() == null ? null : dto.getReason().trim();
        if (status == 0 && (reason == null || reason.isEmpty())) {
            throw new BizException("封禁原因不能为空");
        }

        Long operatorId = requireAdminUserId();
        if (operatorId.equals(id)) {
            throw new BizException("不能封禁自己");
        }

        UserStatusUpdateVO result = userFeignClient.setUserStatus(id, operatorId, dto);
        if (result == null || !result.isSuccess()) {
            throw new BizException(result == null ? "用户服务不可用" : result.getMessage());
        }
    }

    @Override
    public NoticeVO createNotice(NoticeDTO noticeDTO) {
        if (noticeDTO == null) {
            throw new BizException("公告信息不能为空");
        }
        int publishStatus = noticeDTO.getPublishStatus() == null ? NOTICE_DRAFT : noticeDTO.getPublishStatus();
        if (publishStatus != NOTICE_DRAFT && publishStatus != NOTICE_PUBLISHED) {
            throw new BizException("发布状态不合法：0 草稿，1 发布");
        }
        Long publisherId = requireAdminUserId();

        LocalDateTime now = LocalDateTime.now();
        PlatformNotice notice = new PlatformNotice();
        notice.setTitle(noticeDTO.getTitle().trim());
        notice.setContent(noticeDTO.getContent().trim());
        notice.setPublishStatus(publishStatus);
        notice.setPublisherId(publisherId);
        notice.setPublishedAt(publishStatus == NOTICE_PUBLISHED ? now : null);
        notice.setCreatedAt(now);
        notice.setUpdatedAt(now);

        int rows = platformNoticeMapper.insert(notice);
        if (rows <= 0) {
            throw new BizException("保存公告失败");
        }
        return toNoticeVo(notice);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * publishStatus 为空时返回全部公告。
     */
    @Override
    public List<NoticeVO> listNotices(Integer publishStatus) {
        if (publishStatus != null && publishStatus != NOTICE_DRAFT && publishStatus != NOTICE_PUBLISHED) {
            throw new BizException("发布状态不合法：0 草稿，1 发布");
        }
        List<PlatformNotice> rows = platformNoticeMapper.selectAll(publishStatus);
        List<NoticeVO> result = new ArrayList<>();
        if (rows != null) {
            for (PlatformNotice row : rows) {
                result.add(toNoticeVo(row));
            }
        }
        return result;
    }

    /** 公告实体转展示 VO，只映射接口约定的字段。 */
    private NoticeVO toNoticeVo(PlatformNotice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setPublishStatus(notice.getPublishStatus());
        vo.setPublishedAt(notice.getPublishedAt());
        vo.setCreatedAt(notice.getCreatedAt());
        return vo;
    }
}
