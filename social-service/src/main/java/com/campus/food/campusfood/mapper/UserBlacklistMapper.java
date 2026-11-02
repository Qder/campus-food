package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.UserBlacklist;

/**
 * {@code user_blacklist}：建会话/发消息只读 {@code countPair}；黑名单页再写增删列表。
 *
 * @author yang
 * @since 2026-10-31
 */
@Mapper
public interface UserBlacklistMapper {

    /**
     * {@code userId} 是否拉黑了 {@code blockedUid}。建会话、发消息、加好友前双向各查一次。
     *
     * @param userId     拉黑人
     * @param blockedUid 被拉黑人
     * @return 命中条数；大于 0 表示已拉黑
     */
    int countPair(@Param("userId") Long userId, @Param("blockedUid") Long blockedUid);

    /**
     * 插入一行拉黑。{@code created_at} 走表默认值。
     *
     * @param row 须带 userId、blockedUid
     * @return 影响行数
     */
    int insert(UserBlacklist row);

    /**
     * 取消拉黑：只删「我拉黑对方」这一行，对方是否拉黑我不管。
     *
     * @param userId     当前用户
     * @param blockedUid 要移出的用户
     * @return 影响行数；0 表示本来就不在名单里
     */
    int deletePair(@Param("userId") Long userId, @Param("blockedUid") Long blockedUid);

    /**
     * 当前用户拉黑的人，按拉黑时间倒序。
     *
     * @param userId 拉黑人
     * @return 名单；没有返回空列表
     */
    List<UserBlacklist> selectByUserId(@Param("userId") Long userId);
}
