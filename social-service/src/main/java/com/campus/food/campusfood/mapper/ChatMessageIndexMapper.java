package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.campus.food.campusfood.entity.ChatMessageIndex;

/**
 * {@code chat_message_index} Mapper。Mongo 正文落库后再写这一行。
 *
 * @author yang
 * @since 2026-10-31
 */
@Mapper
public interface ChatMessageIndexMapper {

    /**
     * 写入消息索引：{@code mongoMsgId}、会话、发送者、类型、时间。
     *
     * @param row 与 Mongo 文档对齐的索引行
     * @return 影响行数
     */
    int insert(ChatMessageIndex row);
}
