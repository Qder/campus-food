package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.AddFavoritiesDTO;
import com.campus.food.campusfood.dto.AddFeedDTO;
import com.campus.food.campusfood.dto.DeleteFavoritiesDTO;
import com.campus.food.campusfood.dto.ReminderDTO;
import com.campus.food.campusfood.dto.UpdateProfileDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.entity.UserFavorities;
import com.campus.food.campusfood.entity.UserFeedPost;
import com.campus.food.campusfood.entity.UserReminder;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.mapper.UserFavoriteMapper;
import com.campus.food.campusfood.mapper.UserFeedPostMapper;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.mapper.UserReminderMapper;
import com.campus.food.campusfood.util.TagTextUtil;
import com.campus.food.campusfood.service.UserService;
import com.campus.food.campusfood.enums.FoodItemStatus;
import com.campus.food.campusfood.vo.FavoriteAddVO;
import com.campus.food.campusfood.vo.FavoritiesVO;
import com.campus.food.campusfood.vo.FeedPublishVO;
import com.campus.food.campusfood.vo.FeedVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.RemindVO;
import com.campus.food.campusfood.vo.UserProfileVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /** 本期唯一渠道：站内信 */
    private static final String CHANNEL_INBOX = "INBOX";

    /** GET 列表固定顺序：优惠活动、好友动态、订单状态 */
    private static final List<String> REMIND_TYPES = List.of("CAMPAIGN", "FRIEND_FEED", "ORDER");

    /** 展示文案，不入库 */
    private static final Map<String, String> REMIND_LABELS = Map.of(
            "CAMPAIGN", "优惠活动",
            "FRIEND_FEED", "好友动态",
            "ORDER", "订单状态");

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserReminderMapper userReminderMapper;

    @Autowired
    private UserFeedPostMapper userFeedPostMapper;

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
     *
     * <p>
     * 按主键查未删除用户，拷贝 Feign 用的摘要字段，不含密码。
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
     *
     * <p>
     * 入驻审核通过后由商家服务调用：只把 {@code roleId} 写成商家角色，其它字段不动。
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

    /**
     * {@inheritDoc}
     *
     * <p>
     * {@code targetType} 为空查全部；分页默认第 1 页、每页 10 条、最大 50。
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>
     * 校验类型 1/2、目标上架或营业后再插入；已收藏则拒绝。
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>
     * 按当前用户 + 类型 + 目标删除，删不到视为未收藏过。
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>
     * 按 {@link #REMIND_TYPES} 逐项保证库中有行，缺则默认开启站内信。
     */
    @Override
    public List<RemindVO> listReminders() {
        Long userId = requireLoginUserId();
        List<RemindVO> result = new ArrayList<>();
        for (String type : REMIND_TYPES) {
            UserReminder row = ensureReminder(userId, type, CHANNEL_INBOX, 1);
            result.add(toRemindVo(row));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 只改当前用户自己的开关；{@code channel} 空则视为 {@link #CHANNEL_INBOX}。
     */
    @Override
    public RemindVO updateReminder(ReminderDTO dto) {
        Long userId = requireLoginUserId();
        String remindType = dto.getRemindType();
        if (remindType == null || !REMIND_LABELS.containsKey(remindType)) {
            throw new BizException("不支持的提醒类型");
        }
        if (dto.getEnabled() == null) {
            throw new BizException("是否开启不能为空");
        }
        String channel = dto.getChannel() == null || dto.getChannel().isBlank()
                ? CHANNEL_INBOX
                : dto.getChannel();
        if (!CHANNEL_INBOX.equals(channel)) {
            throw new BizException("不支持的提醒类型");
        }
        int enabled = Boolean.TRUE.equals(dto.getEnabled()) ? 1 : 0;
        UserReminder existing = userReminderMapper.selectByUserAndType(userId, remindType);
        if (existing == null) {
            UserReminder row = new UserReminder();
            row.setUserId(userId);
            row.setRemindType(remindType);
            row.setChannel(channel);
            row.setEnabled(enabled);
            int rows = userReminderMapper.insert(row);
            if (rows <= 0) {
                throw new BizException("更新提醒失败");
            }
            return toRemindVo(row);
        }
        int rows = userReminderMapper.updateEnabled(userId, remindType, enabled, channel);
        if (rows <= 0) {
            throw new BizException("更新提醒失败");
        }
        existing.setEnabled(enabled);
        existing.setChannel(channel);
        return toRemindVo(existing);
    }

    /**
     * 从登录上下文取 userId。
     *
     * @return 当前用户 ID
     * @throws BizException 未登录时
     */
    private Long requireLoginUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        return userId;
    }

    /**
     * 该类型尚无记录时插入一行。
     *
     * @param userId     用户 ID
     * @param remindType 提醒类型
     * @param channel    渠道
     * @param enabled    1 开 / 0 关
     * @return 已有或新插入的行
     */
    private UserReminder ensureReminder(Long userId, String remindType, String channel, int enabled) {
        UserReminder existing = userReminderMapper.selectByUserAndType(userId, remindType);
        if (existing != null) {
            return existing;
        }
        UserReminder row = new UserReminder();
        row.setUserId(userId);
        row.setRemindType(remindType);
        row.setChannel(channel);
        row.setEnabled(enabled);
        userReminderMapper.insert(row);
        return row;
    }

    /**
     * 实体转开关 VO；{@code label} 按类型写死，{@code enabled} 由 0/1 转布尔。
     *
     * @param row 库表行
     * @return 前端开关项
     */
    private RemindVO toRemindVo(UserReminder row) {
        RemindVO vo = new RemindVO();
        vo.setRemindType(row.getRemindType());
        vo.setLabel(REMIND_LABELS.get(row.getRemindType()));
        vo.setChannel(row.getChannel());
        vo.setEnabled(row.getEnabled() != null && row.getEnabled() == 1);
        return vo;
    }

    @Override
    public PageVO<FeedVO> listFeeds(Integer page, Integer size) {
        Long userId = requireLoginUserId();
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 50);
        int total = userFeedPostMapper.countByUserId(userId);
        List<FeedVO> list = new ArrayList<>();
        if (total > 0) {
            for (UserFeedPost row : userFeedPostMapper.selectPageByUserId(userId, (p - 1) * s, s)) {
                list.add(toFeedVo(row));
            }
        }
        PageVO<FeedVO> pageVO = new PageVO<>();
        pageVO.setTotal(total);
        pageVO.setSize(s);
        pageVO.setPage(p);
        pageVO.setList(list);
        return pageVO;
    }

    /**
     * 动态实体转列表 VO；库内图片为逗号分隔，接口返回数组。
     *
     * @param row 动态行
     * @return 列表卡片
     */
    private FeedVO toFeedVo(UserFeedPost row) {
        FeedVO vo = new FeedVO();
        vo.setFeedId(row.getId());
        vo.setContent(row.getContent());
        vo.setCreatedAt(row.getCreatedAt());
        vo.setImages(splitImageUrls(row.getImageUrls()));
        vo.setPrivacy(row.getPrivacy());
        vo.setLikeCount(row.getLikeCount() == null ? 0 : row.getLikeCount());
        vo.setCommentCount(0);
        return vo;
    }

    /**
     * 库内逗号字符串 → 接口数组。
     *
     * @param imageUrls 逗号分隔 URL，可为 null
     * @return 图片列表；无图返回空列表
     */
    private List<String> splitImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return List.of();
        }
        List<String> images = new ArrayList<>();
        for (String part : imageUrls.split(",")) {
            String url = part.trim();
            if (!url.isEmpty()) {
                images.add(url);
            }
        }
        return images;
    }

    /**
     * 接口数组 → 库内逗号字符串。
     *
     * @param images 图片 URL 列表，可为 null
     * @return 逗号拼接；无图返回 null
     */
    private String joinImageUrls(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String url : images) {
            if (url == null || url.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(url.trim());
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    @Override
    public FeedPublishVO addFeed(AddFeedDTO dto) {
        Long userId = requireLoginUserId();
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BizException("动态内容不能为空");
        }
        if (dto.getImages() != null && dto.getImages().size() > 9) {
            throw new BizException("最多9张图片");
        }
        Integer privacy = dto.getPrivacy() == null ? 1 : dto.getPrivacy();
        if (privacy != 0 && privacy != 1) {
            throw new BizException("隐私设置不合法");
        }
        UserFeedPost row = new UserFeedPost();
        row.setUserId(userId);
        row.setContent(dto.getContent().trim());
        row.setImageUrls(joinImageUrls(dto.getImages()));
        row.setPrivacy(privacy);
        row.setLikeCount(0);
        int rows = userFeedPostMapper.insert(row);
        if (rows <= 0) {
            throw new BizException("发布动态失败");
        }
        return new FeedPublishVO(row.getId(), 0);
    }

    @Override
    public void deleteFeed(Long id) {
        Long userId = requireLoginUserId();
        UserFeedPost row = userFeedPostMapper.selectById(id);
        if (row == null) {
            throw new BizException("动态不存在");
        }
        if (!row.getUserId().equals(userId)) {
            throw new BizException("无权限删除");
        }
        int deleted = userFeedPostMapper.deleteByIdAndUserId(id, userId);
        if (deleted <= 0) {
            throw new BizException("删除动态失败");
        }
    }
}
