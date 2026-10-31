package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 创建/获取会话成功体。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class ChatSessionVO {

    private Long sessionId;

    private Integer sessionType;

    private Long peerUserId;

    private String peerNickname;

    private String peerAvatarUrl;
}
