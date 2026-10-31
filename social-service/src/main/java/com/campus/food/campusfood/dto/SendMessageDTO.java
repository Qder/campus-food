package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 发送聊天消息入参。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class SendMessageDTO {

    /** TEXT / IMAGE / EMOJI */
    private String msgType;

    /** 文字、图片 URL 或表情码 */
    private String content;

    /** 前端去重 ID */
    private String clientMsgId;
}
