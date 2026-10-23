package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.entity.Friendship;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FriendshipMapper;
import com.campus.food.campusfood.service.FriendshipService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    /** 关注集合：该用户关注的好友 ID */
    private static final String FOLLOW_KEY_PREFIX = "social:follow:";
    /** 粉丝集合：关注该用户的人 ID */
    private static final String FANS_KEY_PREFIX = "social:fans:";

    private final FriendshipMapper friendshipMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public FriendshipServiceImpl(FriendshipMapper friendshipMapper, StringRedisTemplate stringRedisTemplate) {
        this.friendshipMapper = friendshipMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public int insert(Friendship friendship) {
        return friendshipMapper.insert(friendship);
    }

    @Override
    public int update(Friendship friendship) {
        return friendshipMapper.update(friendship);
    }

    @Override
    public int delete(Long id) {
        return friendshipMapper.delete(id);
    }

    @Override
    public Friendship selectById(Long id) {
        return friendshipMapper.selectById(id);
    }

    @Override
    public List<Friendship> selectByUserId(Long userId) {
        return friendshipMapper.selectByUserId(userId);
    }

    @Override
    public List<Friendship> selectAll() {
        return friendshipMapper.selectAll();
    }

    @Override
    public boolean selectByUserIdAndFriendId(Long userId, Long friendId) {
        int rows = friendshipMapper.selectByUserIdAndFriendId(userId, friendId);
        if(rows > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int deletePair(Long userId, Long friendId) {
        Long curId=UserContext.getUserId();
        if(curId.equals(friendId)) {
            return 0;
        }
        if(friendshipMapper.selectByUserIdAndFriendId(userId, friendId) == 0) {
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
            // Redis 挂了就走下面哪一步使用数据库查询数据
        }
        return 1;
    }
}
