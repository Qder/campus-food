package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * {@code user_blacklist} 只读：判断 A 是否拉黑 B。
 *
 * @author yang
 * @since 2026-10-31
 */
@Mapper
public interface UserBlacklistMapper {

    /**
     * {@code userId} 是否拉黑了 {@code blockedUid}。建会话、发消息前双向各查一次。
     *
     * @param userId     拉黑人
     * @param blockedUid 被拉黑人
     * @return 命中条数；大于 0 表示已拉黑
     */
    int countPair(@Param("userId") Long userId, @Param("blockedUid") Long blockedUid);
}
