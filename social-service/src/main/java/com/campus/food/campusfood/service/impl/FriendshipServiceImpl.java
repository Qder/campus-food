package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.Friendship;
import com.campus.food.campusfood.mapper.FriendshipMapper;
import com.campus.food.campusfood.service.FriendshipService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {
    private final FriendshipMapper friendshipMapper;

    public FriendshipServiceImpl(FriendshipMapper friendshipMapper) {
        this.friendshipMapper = friendshipMapper;
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
}
