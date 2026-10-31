package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 创建/获取私聊会话入参。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class CreateSessionDTO {

    /** 对方用户 ID */
    private Long peerUserId;

    /** 1 私聊 / 2 群聊；本期只允许 1 */
    private Integer sessionType;
}
