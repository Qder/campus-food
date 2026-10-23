package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.UserFeedPost;
import com.campus.food.campusfood.mapper.UserFeedPostMapper;
import com.campus.food.campusfood.service.UserFeedPostService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFeedPostServiceImpl implements UserFeedPostService {
    private final UserFeedPostMapper userFeedPostMapper;

    public UserFeedPostServiceImpl(UserFeedPostMapper userFeedPostMapper) {
        this.userFeedPostMapper = userFeedPostMapper;
    }

    @Override
    public int insert(UserFeedPost userFeedPost) {
        return userFeedPostMapper.insert(userFeedPost);
    }

    @Override
    public int update(UserFeedPost userFeedPost) {
        return userFeedPostMapper.update(userFeedPost);
    }

    @Override
    public int delete(Long id) {
        return userFeedPostMapper.delete(id);
    }

    @Override
    public UserFeedPost selectById(Long id) {
        return userFeedPostMapper.selectById(id);
    }

    @Override
    public List<UserFeedPost> selectByUserId(Long userId) {
        return userFeedPostMapper.selectByUserId(userId);
    }

    @Override
    public List<UserFeedPost> selectAll() {
        return userFeedPostMapper.selectAll();
    }
}
