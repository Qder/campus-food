package com.campus.food.campusfood.vo;

import java.util.List;

import lombok.Data;

/**
 * 会话历史消息分页。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class ChatMessagePageVO {

    private Long sessionId;

    private Boolean hasMore;

    private List<ChatMessageVO> list;
}
