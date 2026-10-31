package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 聊天消息正文，对应 MongoDB 集合 {@code chat_message}。
 *
 * <p>字段与 MySQL {@code chat_message_index} 保持同名（下划线转驼峰），
 * 这样写索引表时不用来回翻译；会话与成员关系仍在 MySQL。
 *
 * @author yang
 * @since 2026-10-24
 */
@Data
@Document(collection = "chat_message")
@CompoundIndex(name = "idx_session_time", def = "{'sessionId': 1, 'createdAt': -1}")
public class ChatMessage {

    /** Mongo 主键，同时回写到 MySQL 的 chat_message_index.mongo_msg_id */
    @Id
    private String id;

    /** 会话 ID，对应 MySQL chat_session.id */
    private Long sessionId;

    /** 发送者用户 ID，对应 MySQL chat_message_index.from_user_id */
    private Long fromUserId;

    /** 消息类型：TEXT / IMAGE / EMOJI */
    private String msgType;

    /** 消息正文；图片类消息存 URL */
    private String content;

    /** 前端去重 ID，可为空 */
    private String clientMsgId;

    /** 发送时间，对应 MySQL chat_message_index.created_at */
    private LocalDateTime createdAt;
}
