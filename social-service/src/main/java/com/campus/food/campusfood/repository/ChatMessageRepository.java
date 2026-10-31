package com.campus.food.campusfood.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.campus.food.campusfood.entity.ChatMessage;

/**
 * {@code chat_message} 集合的仓储，方法名由 Spring Data 解析成查询。
 *
 * @author yang
 * @since 2026-10-24
 */
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 按会话取消息，发送时间倒序分页。
     *
     * @param sessionId 会话 ID
     * @param pageable  分页参数
     * @return 消息列表；无数据返回空列表
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);

    /**
     * 取早于指定时间的消息，供上滑加载。
     *
     * @param sessionId 会话 ID
     * @param createdAt 游标时间（该条之前）
     * @param pageable  分页
     * @return 倒序列表
     */
    List<ChatMessage> findBySessionIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long sessionId, java.time.LocalDateTime createdAt, Pageable pageable);
}
