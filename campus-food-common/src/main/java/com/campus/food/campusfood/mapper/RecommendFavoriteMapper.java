package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 推荐用的收藏只读查询，不经过 user-service。
 *
 * @author yang
 * @since 2026-11-08
 */
@Mapper
public interface RecommendFavoriteMapper {

    /**
     * 当前用户收藏的美食 ID（{@code target_type = 1}）。
     *
     * @param userId 用户 ID
     * @return 美食 ID 列表；无收藏返回空列表
     */
    List<Long> selectFoodIdsByUser(@Param("userId") Long userId);
}
