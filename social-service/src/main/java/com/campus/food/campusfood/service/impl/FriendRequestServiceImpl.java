package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.FriendRequest;
import com.campus.food.campusfood.mapper.FriendRequestMapper;
import com.campus.food.campusfood.service.FriendRequestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendRequestServiceImpl implements FriendRequestService {
    private final FriendRequestMapper friendRequestMapper;

    public FriendRequestServiceImpl(FriendRequestMapper friendRequestMapper) {
        this.friendRequestMapper = friendRequestMapper;
    }

    @Override
    public int insert(FriendRequest friendRequest) {
        return friendRequestMapper.insert(friendRequest);
    }

    @Override
    public int update(FriendRequest friendRequest) {
        return friendRequestMapper.update(friendRequest);
    }

    @Override
    public int delete(Long id) {
        return friendRequestMapper.delete(id);
    }

    @Override
    public FriendRequest selectById(Long id) {
        return friendRequestMapper.selectById(id);
    }

    @Override
    public List<FriendRequest> selectByUserId(Long userId) {
        return friendRequestMapper.selectByUserId(userId);
    }

    @Override
    public List<FriendRequest> selectAll() {
        return friendRequestMapper.selectAll();
    }
}
