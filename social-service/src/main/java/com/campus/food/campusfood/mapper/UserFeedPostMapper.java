package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.UserFeedPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFeedPostMapper {
    int insert(UserFeedPost userFeedPost);
    int update(UserFeedPost userFeedPost);
    int delete(Long id);
    UserFeedPost selectById(Long id);
    List<UserFeedPost> selectByUserId(Long userId);
    List<UserFeedPost> selectAll();
}
