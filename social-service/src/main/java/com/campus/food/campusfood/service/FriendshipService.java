package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.Friendship;

import java.util.List;

/**
 * 好友关系服务。
 *
 * <p>关系在库中双向各存一行；删除好友时同时清理 Redis 的关注/粉丝集合。
 *
 * @author yang
 * @since 2026-10-23
 */
public interface FriendshipService {

    /**
     * 新增单向好友关系。
     *
     * @param friendship 待插入的关系
     * @return 影响行数
     */
    int insert(Friendship friendship);

    /**
     * 更新好友关系（如修改备注名）。
     *
     * @param friendship 含主键的待更新关系
     * @return 影响行数
     */
    int update(Friendship friendship);

    /**
     * 按主键删除单向关系。
     *
     * @param id 关系 ID
     * @return 影响行数
     */
    int delete(Long id);

    /**
     * 按主键查询关系。
     *
     * @param id 关系 ID
     * @return 关系记录；不存在返回 null
     */
    Friendship selectById(Long id);

    /**
     * 查询某人关注的好友列表。
     *
     * @param userId 用户 ID
     * @return 关系列表；无数据返回空列表
     */
    List<Friendship> selectByUserId(Long userId);

    /**
     * 查询全部关系。
     *
     * @return 关系列表；无数据返回空列表
     */
    List<Friendship> selectAll();

    /**
     * 判断与目标用户是否已是好友。
     *
     * @param userId   用户 ID
     * @param friendId 目标用户 ID
     * @return true 表示已是好友
     */
    boolean selectByUserIdAndFriendId(Long userId, Long friendId);

    /**
     * 删除好友：双向删库，并同步清理 Redis 的关注/粉丝集合。
     *
     * @param userId   用户 ID
     * @param friendId 好友用户 ID
     * @return 1 表示删除成功；0 表示目标即自己，未做任何改动
     * @throws com.campus.food.campusfood.exception.BizException 好友关系不存在时
     */
    int deletePair(Long userId, Long friendId);
}
