package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.Friendship;

import java.util.List;

public interface FriendshipService {
    int insert(Friendship friendship);

    int update(Friendship friendship);

    int delete(Long id);

    Friendship selectById(Long id);

    List<Friendship> selectByUserId(Long userId);

    List<Friendship> selectAll();

    boolean selectByUserIdAndFriendId(Long userId, Long friendId);

    int deletePair(Long userId, Long friendId);
}
