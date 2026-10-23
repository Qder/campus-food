package com.campus.food.campusfood.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.entity.FriendRequest;
import com.campus.food.campusfood.mapper.FriendRequestMapper;
import com.campus.food.campusfood.mapper.FriendshipMapper;
import com.campus.food.campusfood.mapper.UserFeedPostMapper;
import com.campus.food.campusfood.service.SocialService;
import com.campus.food.campusfood.vo.FriendRequestVo;

@Service
public class SocialServiceImpl implements SocialService {

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private UserFeedPostMapper userFeedPostMapper;

    @Override
    public FriendRequestVo sendFriendRequest(FriendRequest friendRequest) {
        friendRequestMapper.insert(friendRequest);
        return friendRequestMapper.selectById(friendRequest.getId());
    }

    @Override
    public FriendRequestVo handleFriendRequest(FriendRequestVo friendRequestVo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleFriendRequest'");
    }
    
}
