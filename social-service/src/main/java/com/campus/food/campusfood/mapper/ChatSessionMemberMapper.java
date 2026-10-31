package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.ChatSessionMember;

/**
 * {@code chat_session_member} Mapper。私聊插入两行（自己、对方）。
 *
 * @author yang
 * @since 2026-10-31
 */
@Mapper
public interface ChatSessionMemberMapper {

    /**
     * 加入会话。私聊建会话时各插一行。
     *
     * @param row 须带 sessionId、userId；unreadCount 一般 0
     * @return 影响行数
     */
    int insert(ChatSessionMember row);

    /**
     * 该用户是否已是会话成员（大于 0 才允许拉历史、发信）。
     *
     * @param sessionId 会话
     * @param userId    用户
     * @return 命中条数
     */
    int countMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 私聊会话里除自己外的另一人。
     *
     * @param sessionId 会话
     * @param userId    当前用户
     * @return 对方用户 ID；没有返回 null
     */
    Long selectPeerUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
