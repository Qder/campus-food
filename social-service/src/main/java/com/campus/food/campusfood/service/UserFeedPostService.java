package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.UserFeedPost;

import java.util.List;

public interface UserFeedPostService {
    int insert(UserFeedPost userFeedPost);

    int update(UserFeedPost userFeedPost);

    int delete(Long id);

    UserFeedPost selectById(Long id);

    List<UserFeedPost> selectByUserId(Long userId);

    List<UserFeedPost> selectAll();

    List<UserFeedPost> selectByIds(List<Long> ids);
}
