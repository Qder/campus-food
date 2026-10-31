package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 聊天消息对外视图。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class ChatMessageVO {

    private String msgId;

    private String clientMsgId;

    private Long sessionId;

    private Long fromUserId;

    private String fromNickname;

    private String msgType;

    private String content;

    private LocalDateTime createdAt;
}
