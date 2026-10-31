package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 会话成员，对应表 {@code chat_session_member}。
 *
 * <p>私聊、群聊共用：一人一行。私聊固定两行（自己、对方），不是多人会话。
 *
 * @author yang
 * @since 2026-10-24
 */
@Data
public class ChatSessionMember {

    private Long id;

    /** 所属会话，对应 {@code chat_session.id} */
    private Long sessionId;

    /** 这个座位上的用户 */
    private Long userId;

    /**
     * 该用户在本会话的未读条数。
     * 建会话时写 0；发信/已读尚未改这列。
     */
    private Integer unreadCount;

    private LocalDateTime joinedAt;
}
