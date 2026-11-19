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

    /** 群名称；私聊为 null */
    private String title;

    /** 成员数：私聊固定 2，群聊为 1 + memberIds 去重后人数 */
    private Integer memberCount;

    private Long peerUserId;

    private String peerNickname;

    private String peerAvatarUrl;
}
