package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.UserFeedPost;

import feign.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * {@code user_feed_post} 表 MyBatis Mapper。
 *
 * <p>常用索引（仅注释提示，不自动生成方法）：
 * - INDEX idx_feed_user_time: user_id, created_at → 按人取时间线
 * - INDEX idx_feed_food: food_id → 按美食聚合动态
 *
 * @author yang
 * @since 2026-10-23
 */
@Mapper
public interface UserFeedPostMapper {

    /**
     * 新增动态。
     *
     * @param userFeedPost 待插入的动态
     * @return 影响行数
     */
    int insert(UserFeedPost userFeedPost);

    /**
     * 按主键更新动态（动态 SQL，仅更新非 null 字段）。
     *
     * @param userFeedPost 含主键的待更新动态
     * @return 影响行数
     */
    int update(UserFeedPost userFeedPost);

    /**
     * 按主键删除动态。
     *
     * @param id 动态 ID
     * @return 影响行数
     */
    int delete(Long id);

    /**
     * 按主键查询动态。
     *
     * @param id 动态 ID
     * @return 动态记录；不存在返回 null
     */
    UserFeedPost selectById(Long id);

    /**
     * 查询某人发布的动态，按发布时间倒序。
     *
     * @param userId 发布用户 ID
     * @return 动态列表；无数据返回空列表
     */
    List<UserFeedPost> selectByUserId(Long userId);

    /**
     * 查询全部动态，按发布时间倒序。
     *
     * @return 动态列表；无数据返回空列表
     */
    List<UserFeedPost> selectAll();

    /**
     * 按 ID 集合批量查询动态，供 Redis 时间线回表。
     *
     * @param ids 动态 ID 列表
     * @return 动态列表；无数据返回空列表
     */
    List<UserFeedPost> selectByIds(@Param("ids") List<Long> ids);
}
