package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 聊天会话，对应表 {@code chat_session}。
 *
 * <p>一场对话一行。{@code sessionType=1} 为私聊（两人），{@code 2} 为群聊。
 * 成员名单在 {@code chat_session_member}，不在本表。
 *
 * @author yang
 * @since 2026-10-24
 */
@Data
public class ChatSession {

    private Long id;

    /** 1 私聊 / 2 群聊；本期接口只建 1 */
    private Integer sessionType;

    /** 私聊时创建人眼里的对方用户 ID，冗余字段；查已有会话以成员表为准 */
    private Long peerUserId;

    /** 群聊 ID；私聊为 null */
    private Long groupId;

    /** 最后一条消息摘要，会话列表预览用 */
    private String lastMsgPreview;

    /** 最后一条消息时间 */
    private LocalDateTime lastMsgAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
