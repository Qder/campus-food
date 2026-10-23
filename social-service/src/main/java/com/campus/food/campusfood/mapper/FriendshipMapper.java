package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.Friendship;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendshipMapper {
    int insert(Friendship friendship);
    int update(Friendship friendship);
    int delete(Long id);
    Friendship selectById(Long id);
    List<Friendship> selectByUserId(Long userId);
    List<Friendship> selectAll();
    int selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
    int deletePair(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
