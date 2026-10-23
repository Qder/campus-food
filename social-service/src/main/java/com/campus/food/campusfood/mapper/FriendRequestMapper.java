package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.FriendRequest;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendRequestMapper {
    int insert(FriendRequest friendRequest);
    int update(FriendRequest friendRequest);
    int delete(Long id);
    FriendRequest selectById(Long id);
    List<FriendRequest> selectByUserId(Long userId);
    List<FriendRequest> selectAll();
    int selectPending(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
    List<FriendRequest> selectInbox(@Param("toUserId") Long toUserId, @Param("status") Integer status);
}
