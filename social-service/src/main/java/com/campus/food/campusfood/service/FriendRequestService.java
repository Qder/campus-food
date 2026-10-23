package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.FriendRequest;

import java.util.List;

public interface FriendRequestService {
    int insert(FriendRequest friendRequest);

    int update(FriendRequest friendRequest);

    int delete(Long id);

    FriendRequest selectById(Long id);

    List<FriendRequest> selectByUserId(Long userId);

    List<FriendRequest> selectAll();

    List<FriendRequest> selectInbox(Long touserId, Integer status);

    boolean selectPending(Long fromuserId, Long touserId);
}
