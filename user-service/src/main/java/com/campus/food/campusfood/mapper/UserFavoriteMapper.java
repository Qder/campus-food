package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.UserFavorities;
import com.campus.food.campusfood.vo.FavoritiesVO;

/**
 * {@code user_favorite} 表 Mapper。收藏美食/店铺同一张表，用 target_type 区分。
 *
 * @author yang
 * @since 2026-10-27
 */
@Mapper
public interface UserFavoriteMapper {

    /**
     * 新增一条收藏。
     *
     * @param row 含 userId、targetType、targetId
     * @return 影响行数
     */
    int insert(UserFavorities row);

    /**
     * 按当前用户 + 类型 + 目标删除。
     *
     * @param userId     用户 ID
     * @param targetType 1 美食 / 2 店铺
     * @param targetId   美食或店铺 ID
     * @return 影响行数
     */
    int deleteByUserAndTarget(@Param("userId") Long userId,
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId);

    /**
     * 统计当前用户收藏数，供分页 total。
     *
     * @param userId     用户 ID
     * @param targetType 可为 null，表示不按类型过滤
     * @return 条数
     */
    int countByUser(@Param("userId") Long userId, @Param("targetType") Integer targetType);

    /**
     * 分页查询收藏卡片，LEFT JOIN 美食/店铺展示字段。
     *
     * @param userId     用户 ID
     * @param targetType 可为 null
     * @param offset     偏移
     * @param size       条数
     * @return 当前页；无数据返回空列表
     */
    List<FavoritiesVO> selectPageByUser(@Param("userId") Long userId,
            @Param("targetType") Integer targetType,
            @Param("offset") int offset,
            @Param("size") int size);

    /**
     * 统计是否已收藏（唯一键）。
     *
     * @param userId     用户 ID
     * @param targetType 1 美食 / 2 店铺
     * @param targetId   目标 ID
     * @return 命中条数
     */
    int countByUk(@Param("userId") Long userId,
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId);

}
