package com.campus.food.campusfood.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.entity.FriendRequest;
import com.campus.food.campusfood.entity.Friendship;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FriendRequestMapper;
import com.campus.food.campusfood.mapper.FriendshipMapper;
import com.campus.food.campusfood.service.SocialService;
import com.campus.food.campusfood.vo.FriendRequestVo;

import feign.FeignException;

/**
 * 社交业务门面实现：好友申请与处理。
 *
 * <p>申请阶段的判重与隐私校验、同意阶段的双向关系与 Redis 关注集合
 * 都在这一层完成，底层 Mapper 只负责单表读写。
 *
 * @author yang
 * @since 2026-10-23
 */
@Service
public class SocialServiceImpl implements SocialService {

    /** 关注集合：该用户关注的好友 ID */
    private static final String FOLLOW_KEY_PREFIX = "social:follow:";
    /** 粉丝集合：关注该用户的人 ID */
    private static final String FANS_KEY_PREFIX = "social:fans:";

    /** 申请状态：待处理 */
    private static final Integer STATUS_PENDING = 0;
    /** 申请状态：已同意 */
    private static final Integer STATUS_ACCEPTED = 1;
    /** 申请状态：已拒绝 */
    private static final Integer STATUS_REJECTED = 2;

    private final FriendRequestMapper friendRequestMapper;
    private final FriendshipMapper friendshipMapper;
    private final UserFeignClient userFeignClient;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造注入 Mapper、Feign 客户端与 Redis 模板。
     *
     * @param friendRequestMapper  好友申请 Mapper
     * @param friendshipMapper     好友关系 Mapper
     * @param userFeignClient      用户服务内部接口
     * @param stringRedisTemplate  Redis 字符串模板
     */
    public SocialServiceImpl(FriendRequestMapper friendRequestMapper,
                             FriendshipMapper friendshipMapper,
                             UserFeignClient userFeignClient,
                             StringRedisTemplate stringRedisTemplate) {
        this.friendRequestMapper = friendRequestMapper;
        this.friendshipMapper = friendshipMapper;
        this.userFeignClient = userFeignClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * {@inheritDoc}
     *
     * <p>申请人一律取 {@link UserContext}，入参中的 {@code fromUserId} 被忽略；
     * 申请状态由服务端置为待处理。
     */
    @Override
    public FriendRequestVo sendFriendRequest(FriendRequestVo friendRequestVo) {
        Long curId = requireCurUserId();
        if (friendRequestVo == null || friendRequestVo.getToUserId() == null) {
            throw new BizException("好友ID不能为空");
        }
        Long toUserId = friendRequestVo.getToUserId();
        if (curId.equals(toUserId)) {
            throw new BizException("不能添加自己为好友");
        }

        UserInfo target = loadUser(toUserId);
        if (target.getDeleted() != null && target.getDeleted() != 0) {
            throw new BizException("用户不存在");
        }
        if (!Integer.valueOf(1).equals(target.getPrivacyAddFriend())) {
            throw new BizException("对方不允许添加好友");
        }

        if (friendshipMapper.selectByUserIdAndFriendId(curId, toUserId) > 0) {
            throw new BizException("已经是好友");
        }
        // 任一方向存在待处理申请都不再新建，避免同一条关系反复申请
        if (friendRequestMapper.selectPending(curId, toUserId) > 0
                || friendRequestMapper.selectPending(toUserId, curId) > 0) {
            throw new BizException("已有待处理的申请");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(curId);
        request.setToUserId(toUserId);
        request.setMessage(friendRequestVo.getMessage());
        request.setStatus(STATUS_PENDING);
        friendRequestMapper.insert(request);

        return toVo(friendRequestMapper.selectById(request.getId()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>先落库再写 Redis；库写失败由事务回滚，Redis 失败只记日志，
     * 后续读取以数据库为准。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FriendRequestVo handleFriendRequest(FriendRequestVo friendRequestVo) {
        Long curId = requireCurUserId();
        if (friendRequestVo == null || friendRequestVo.getId() == null) {
            throw new BizException("申请ID不能为空");
        }
        Integer targetStatus = friendRequestVo.getStatus();
        if (!STATUS_ACCEPTED.equals(targetStatus) && !STATUS_REJECTED.equals(targetStatus)) {
            throw new BizException("处理结果不合法");
        }

        FriendRequest request = friendRequestMapper.selectById(friendRequestVo.getId());
        if (request == null) {
            throw new BizException("申请不存在");
        }
        if (!curId.equals(request.getToUserId())) {
            throw new BizException("无权处理该申请");
        }
        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new BizException("申请已处理");
        }

        if (STATUS_ACCEPTED.equals(targetStatus)) {
            addFriendPair(request.getFromUserId(), request.getToUserId());
        }

        request.setStatus(targetStatus);
        request.setHandledAt(LocalDateTime.now());
        friendRequestMapper.update(request);

        return toVo(friendRequestMapper.selectById(request.getId()));
    }

    /**
     * 建立双向好友关系，并同步 Redis 关注/粉丝集合。
     *
     * @param fromUserId 申请人
     * @param toUserId   同意人
     */
    private void addFriendPair(Long fromUserId, Long toUserId) {
        // 已有关系时只补推导结果，避免唯一键冲突
        if (friendshipMapper.selectByUserIdAndFriendId(fromUserId, toUserId) == 0) {
            friendshipMapper.insert(buildFriendship(fromUserId, toUserId));
        }
        if (friendshipMapper.selectByUserIdAndFriendId(toUserId, fromUserId) == 0) {
            friendshipMapper.insert(buildFriendship(toUserId, fromUserId));
        }
        try {
            stringRedisTemplate.opsForSet()
                    .add(FOLLOW_KEY_PREFIX + fromUserId, String.valueOf(toUserId));
            stringRedisTemplate.opsForSet()
                    .add(FANS_KEY_PREFIX + toUserId, String.valueOf(fromUserId));
            stringRedisTemplate.opsForSet()
                    .add(FOLLOW_KEY_PREFIX + toUserId, String.valueOf(fromUserId));
            stringRedisTemplate.opsForSet()
                    .add(FANS_KEY_PREFIX + fromUserId, String.valueOf(toUserId));
        } catch (Exception ignored) {
            // Redis 不可用时忽略，读取时回源数据库
        }
    }

    /**
     * 构造单向好友关系。
     *
     * @param userId   用户 ID
     * @param friendId 好友 ID
     * @return 待插入的关系
     */
    private Friendship buildFriendship(Long userId, Long friendId) {
        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        return friendship;
    }

    /**
     * 取当前登录用户 ID。
     *
     * @return 当前用户 ID
     * @throws BizException 未登录时
     */
    private Long requireCurUserId() {
        Long curId = UserContext.getUserId();
        if (curId == null) {
            throw new BizException("请先登录");
        }
        return curId;
    }

    /**
     * 通过 user-service 查询用户摘要。
     *
     * @param userId 用户 ID
     * @return 用户摘要
     * @throws BizException 用户不存在或用户服务不可用时
     */
    private UserInfo loadUser(Long userId) {
        UserInfo user;
        try {
            user = userFeignClient.getUserInfo(userId);
        } catch (FeignException e) {
            // user-service 对不存在用户返回 400，统一转成业务提示
            throw new BizException("用户不存在");
        }
        if (user == null || user.getId() == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /**
     * 实体转对外视图。
     *
     * @param request 申请实体
     * @return 对外视图；入参为 null 时返回 null
     */
    private FriendRequestVo toVo(FriendRequest request) {
        if (request == null) {
            return null;
        }
        FriendRequestVo vo = new FriendRequestVo();
        vo.setId(request.getId());
        vo.setFromUserId(request.getFromUserId());
        vo.setToUserId(request.getToUserId());
        vo.setMessage(request.getMessage());
        vo.setStatus(request.getStatus());
        vo.setCreatedAt(request.getCreatedAt());
        vo.setHandledAt(request.getHandledAt());
        return vo;
    }
}
