package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.UserFeedPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * {@code user_feed_post} 表 MyBatis Mapper。
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
    int delete(@Param("id") Long id);

    /**
     * 按主键 + 发布者删除，避免删别人的动态。
     *
     * @param id     动态 ID
     * @param userId 发布者用户 ID
     * @return 影响行数
     */
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 按主键查询动态。
     *
     * @param id 动态 ID
     * @return 动态记录；不存在返回 null
     */
    UserFeedPost selectById(@Param("id") Long id);

    /**
     * 查询某人发布的动态，按发布时间倒序。
     *
     * @param userId 发布用户 ID
     * @return 动态列表；无数据返回空列表
     */
    List<UserFeedPost> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计某人动态条数，供分页 total。
     *
     * @param userId 发布用户 ID
     * @return 条数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 分页查询某人动态。
     *
     * @param userId 发布用户 ID
     * @param offset 偏移
     * @param size   条数
     * @return 当前页；无数据返回空列表
     */
    List<UserFeedPost> selectPageByUserId(@Param("userId") Long userId,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

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
