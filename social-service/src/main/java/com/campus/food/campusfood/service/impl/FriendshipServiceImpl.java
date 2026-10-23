package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.entity.Friendship;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FriendshipMapper;
import com.campus.food.campusfood.service.FriendshipService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友关系服务实现。
 *
 * <p>
 * 关系以双向两行的形式落库；删除好友时额外清理 Redis 的
 * {@code social:follow:*}（关注）与 {@code social:fans:*}（粉丝）集合。
 * Redis 清理失败直接忽略，以数据库结果为准，后续读取回源重建即可。
 *
 * @author yang
 * @since 2026-10-23
 */
@Service
public class FriendshipServiceImpl implements FriendshipService {

    /** 关注集合：该用户关注的好友 ID */
    private static final String FOLLOW_KEY_PREFIX = "social:follow:";
    /** 粉丝集合：关注该用户的人 ID */
    private static final String FANS_KEY_PREFIX = "social:fans:";

    private final FriendshipMapper friendshipMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造注入 Mapper 与 Redis 模板。
     *
     * @param friendshipMapper    好友关系 Mapper
     * @param stringRedisTemplate Redis 字符串模板
     */
    public FriendshipServiceImpl(FriendshipMapper friendshipMapper, StringRedisTemplate stringRedisTemplate) {
        this.friendshipMapper = friendshipMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** {@inheritDoc} */
    @Override
    public int insert(Friendship friendship) {
        return friendshipMapper.insert(friendship);
    }

    /** {@inheritDoc} */
    @Override
    public int update(Friendship friendship) {
        return friendshipMapper.update(friendship);
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Long id) {
        return friendshipMapper.delete(id);
    }

    /** {@inheritDoc} */
    @Override
    public Friendship selectById(Long id) {
        return friendshipMapper.selectById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Friendship> selectByUserId(Long userId) {
        return friendshipMapper.selectByUserId(userId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Friendship> selectAll() {
        return friendshipMapper.selectAll();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Mapper 返回命中条数，这里换算成布尔值。
     */
    @Override
    public boolean selectByUserIdAndFriendId(Long userId, Long friendId) {
        int rows = friendshipMapper.selectByUserIdAndFriendId(userId, friendId);
        if (rows > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 先确认单向关系存在，再成对删除两个方向；库删除成功后才清 Redis，
     * 避免缓存已清而库仍在的不一致。
     */
    @Override
    public int deletePair(Long userId, Long friendId) {
        Long curId = UserContext.getUserId();
        if (!curId.equals(userId)) {
            throw new BizException("无权操作");
        }

        if (friendId == null) {
            throw new BizException("好友ID不能为空");
        }
        // 防呆：不允许把当前用户自己当作好友删除
        if (curId.equals(friendId)) {
            throw new BizException("不允许把当前用户自己当作好友删除");
        }
        
        if (friendshipMapper.selectByUserIdAndFriendId(userId, friendId) == 0) {
            throw new BizException("好友关系不存在");
        }
        friendshipMapper.deletePair(userId, friendId);
        friendshipMapper.deletePair(friendId, userId);
        // 库已断开双向关系；Redis Set 失败不影响删除结果
        try {
            String uid = String.valueOf(userId);
            String fid = String.valueOf(friendId);
            stringRedisTemplate.opsForSet().remove(FOLLOW_KEY_PREFIX + userId, fid);
            stringRedisTemplate.opsForSet().remove(FANS_KEY_PREFIX + friendId, uid);
            stringRedisTemplate.opsForSet().remove(FOLLOW_KEY_PREFIX + friendId, uid);
            stringRedisTemplate.opsForSet().remove(FANS_KEY_PREFIX + userId, fid);
        } catch (Exception ignored) {
            // Redis 不可用时忽略，后续读取回源数据库即可
        }
        return 1;
    }
}
