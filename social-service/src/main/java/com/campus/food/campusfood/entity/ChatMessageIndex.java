package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 聊天消息索引，对应表 {@code chat_message_index}。
 *
 * <p>MySQL 只存检索字段；正文在 Mongo {@code chat_message}，主键字符串回写到 {@code mongoMsgId}。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class ChatMessageIndex {

    private Long id;

    /** Mongo {@code chat_message._id} 字符串 */
    private String mongoMsgId;

    private Long sessionId;

    /** 发送者用户 ID */
    private Long fromUserId;

    /** TEXT / IMAGE / EMOJI */
    private String msgType;

    private LocalDateTime createdAt;
}
