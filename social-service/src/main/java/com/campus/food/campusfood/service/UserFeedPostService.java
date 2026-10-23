package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.UserFeedPost;

import java.util.List;

/**
 * 用户美食动态（Feed）服务。
 *
 * <p>动态正文落库；时间线由 Redis 维护，读时间线后按 ID 批量回表。
 *
 * @author yang
 * @since 2026-10-23
 */
public interface UserFeedPostService {

    /**
     * 新增动态。
     *
     * @param userFeedPost 待插入的动态
     * @return 影响行数
     */
    int insert(UserFeedPost userFeedPost);

    /**
     * 更新动态（如编辑正文、图片）。
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
     * 查询某人发布的动态，按时间倒序。
     *
     * @param userId 发布用户 ID
     * @return 动态列表；无数据返回空列表
     */
    List<UserFeedPost> selectByUserId(Long userId);

    /**
     * 查询全部动态，按时间倒序。
     *
     * @return 动态列表；无数据返回空列表
     */
    List<UserFeedPost> selectAll();

    /**
     * 按 ID 集合批量查询动态，供时间线回表。
     *
     * @param ids 动态 ID 列表，可为 null 或空
     * @return 动态列表；入参为空时返回空列表
     */
    List<UserFeedPost> selectByIds(List<Long> ids);
}
