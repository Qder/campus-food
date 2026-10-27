package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.AddFavoritiesDTO;
import com.campus.food.campusfood.dto.DeleteFavoritiesDTO;
import com.campus.food.campusfood.dto.UpdateProfileDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.entity.UserFavorities;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.mapper.UserFavoriteMapper;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.util.TagTextUtil;
import com.campus.food.campusfood.service.UserService;
import com.campus.food.campusfood.enums.FoodItemStatus;
import com.campus.food.campusfood.vo.FavoriteAddVO;
import com.campus.food.campusfood.vo.FavoritiesVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.UserProfileVO;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 当前登录用户资料服务实现。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class UserServiceImpl implements UserService {

    /** 库表商家角色 ID，与权限设计一致 */
    private static final Integer ROLE_MERCHANT = 2;

    /** 店铺审核通过 */
    private static final int MERCHANT_AUDIT_APPROVED = 1;

    /** 店铺营业中 */
    private static final int MERCHANT_STATUS_OPEN = 1;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * {@inheritDoc}
     *
     * <p>
     * 从 {@link UserContext} 取 userId 后查询。
     */
    @Override
    public UserProfileVO getCurrentProfile() {
        return toProfileVo(requireCurrentUser());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 采用动态更新：只把 DTO 中非 null 的字段写进 patch。
     */
    @Override
    public UserProfileVO updateCurrentProfile(UpdateProfileDTO dto) {
        User existing = requireCurrentUser();
        User patch = buildProfilePatch(existing.getId(), dto);
        if (!hasProfilePatch(patch)) {
            return toProfileVo(existing);
        }

        int rows = userMapper.updateById(patch);
        if (rows <= 0) {
            throw new BizException("更新失败");
        }

        User refreshed = userMapper.selectById(existing.getId());
        return toProfileVo(refreshed != null ? refreshed : existing);
    }

    /**
     * 加载当前登录用户。
     *
     * @return 当前登录用户
     * @throws BizException 未登录或用户已删除时
     */
    private User requireCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() != 0)) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /**
     * 仅把 DTO 中非 null 字段写入 patch，供动态 SQL 按列更新。
     *
     * @param userId 当前用户 ID
     * @param dto    更新请求
     * @return 只含待更新列的 patch 对象
     * @throws BizException 昵称或口味被传空时
     */
    private User buildProfilePatch(Long userId, UpdateProfileDTO dto) {
        User patch = new User();
        patch.setId(userId);
        if (dto.getNickname() != null) {
            String nickname = dto.getNickname().trim();
            if (nickname.isEmpty()) {
                throw new BizException("昵称不能为空");
            }
            patch.setNickname(nickname);
        }
        if (dto.getAvatarUrl() != null) {
            patch.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getGender() != null) {
            patch.setGender(dto.getGender());
        }
        if (dto.getSchool() != null) {
            patch.setSchool(dto.getSchool());
        }
        if (dto.getMajor() != null) {
            patch.setMajor(dto.getMajor());
        }
        if (dto.getBio() != null) {
            patch.setBio(dto.getBio());
        }
        if (dto.getPrivacyFeed() != null) {
            patch.setPrivacyFeed(dto.getPrivacyFeed());
        }
        if (dto.getPrivacyAddFriend() != null) {
            patch.setPrivacyAddFriend(dto.getPrivacyAddFriend());
        }
        if (dto.getTasteTags() != null) {
            String tasteTags = TagTextUtil.joinTags(TagTextUtil.parseTags(dto.getTasteTags()));
            if (tasteTags.isEmpty()) {
                throw new BizException("口味偏好不能为空");
            }
            patch.setTasteTags(tasteTags);
        }
        if (dto.getDislikeIngredients() != null) {
            patch.setDislikeIngredients(dto.getDislikeIngredients());
        }
        return patch;
    }

    /**
     * 判断 patch 是否含任何待更新字段。
     *
     * @param patch 待更新字段集合
     * @return 至少有一个非 null 字段返回 true
     */
    private boolean hasProfilePatch(User patch) {
        return patch.getNickname() != null
                || patch.getAvatarUrl() != null
                || patch.getGender() != null
                || patch.getSchool() != null
                || patch.getMajor() != null
                || patch.getBio() != null
                || patch.getPrivacyFeed() != null
                || patch.getPrivacyAddFriend() != null
                || patch.getTasteTags() != null
                || patch.getDislikeIngredients() != null;
    }

    /**
     * 实体转资料 VO，只映射可对外暴露的字段。
     *
     * @param user 用户实体
     * @return 不含密码的资料 VO
     */
    private UserProfileVO toProfileVo(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setGender(user.getGender());
        vo.setSchool(user.getSchool());
        vo.setMajor(user.getMajor());
        vo.setBio(user.getBio());
        vo.setPrivacyFeed(user.getPrivacyFeed());
        vo.setPrivacyAddFriend(user.getPrivacyAddFriend());
        vo.setRoleId(user.getRoleId());
        vo.setStatus(user.getStatus());
        vo.setTasteTags(user.getTasteTags());
        vo.setDislikeIngredients(user.getDislikeIngredients());
        return vo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfo getUserInfo(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || (user.getDeleted() != null && user.getDeleted() != 0)) {
            throw new BizException("用户不存在");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setTasteTags(user.getTasteTags());
        userInfo.setDislikeIngredients(user.getDislikeIngredients());
        userInfo.setDeleted(user.getDeleted());
        userInfo.setRoleId(user.getRoleId());
        userInfo.setPrivacyAddFriend(user.getPrivacyAddFriend());
        return userInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMerchantRole(Long id) {
        User user = new User();
        user.setId(id);
        user.setRoleId(ROLE_MERCHANT);
        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BizException("设置用户角色失败");
        }
    }

    @Override
    public PageVO<FavoritiesVO> getFavoritiesList(Integer targetType, Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }

        if (targetType != null && targetType != 1 && targetType != 2) {
            throw new BizException("目标类型不合法");
        }
        // targetType传入为null时，默认查询所有类型
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 50);
        int total = userFavoriteMapper.countByUser(userId, targetType);
        List<FavoritiesVO> list = total == 0
                ? List.of()
                : userFavoriteMapper.selectPageByUser(userId, targetType, (p - 1) * s, s);
        PageVO<FavoritiesVO> pageVO = new PageVO<>();
        pageVO.setTotal(total);
        pageVO.setSize(s);
        pageVO.setPage(p);
        pageVO.setList(list);
        return pageVO;
    }

    @Override
    public FavoriteAddVO addFavorities(AddFavoritiesDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        Integer targetType = dto.getTargetType();
        Long targetId = dto.getTargetId();
        if (targetType == null || (targetType != 1 && targetType != 2) || targetId == null) {
            throw new BizException("目标类型不合法");
        }
        if (targetType == 1) {
            FoodItem foodItem = foodItemMapper.selectById(targetId);
            if (foodItem == null) {
                throw new BizException("该美食不存在");
            }
            if (foodItem.getStatus() == null
                    || foodItem.getStatus() != FoodItemStatus.ON_SHELF.getCode()) {
                throw new BizException("该美食已下架");
            }
        } else {
            Merchant merchant = merchantMapper.selectById(targetId);
            if (merchant == null) {
                throw new BizException("该店铺不存在");
            }
            if (!Integer.valueOf(MERCHANT_AUDIT_APPROVED).equals(merchant.getAuditStatus())
                    || !Integer.valueOf(MERCHANT_STATUS_OPEN).equals(merchant.getStatus())) {
                throw new BizException("该店铺已下架");
            }
        }

        if (userFavoriteMapper.countByUk(userId, targetType, targetId) > 0) {
            throw new BizException("已经收藏过了");
        }

        UserFavorities userFavorities = new UserFavorities();
        userFavorities.setUserId(userId);
        userFavorities.setTargetType(targetType);
        userFavorities.setTargetId(targetId);

        int rows = userFavoriteMapper.insert(userFavorities);
        if (rows <= 0) {
            throw new BizException("添加收藏失败");
        }
        return new FavoriteAddVO(targetType, targetId, true);
    }

    @Override
    public FavoriteAddVO deleteFavorities(DeleteFavoritiesDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        Integer targetType = dto.getTargetType();
        Long targetId = dto.getTargetId();
        if (targetType == null || (targetType != 1 && targetType != 2) || targetId == null) {
            throw new BizException("目标类型不合法");
        }
        int rows = userFavoriteMapper.deleteByUserAndTarget(userId, targetType, targetId);
        if (rows <= 0) {
            throw new BizException("未收藏过");
        }
        return new FavoriteAddVO(targetType, targetId, false);
    }
}
