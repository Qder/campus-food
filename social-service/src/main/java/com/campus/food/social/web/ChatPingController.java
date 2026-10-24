package com.campus.food.social.web;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.entity.ChatMessage;
import com.campus.food.campusfood.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

/**
 * MongoDB 连通性冒烟接口：写入一条消息、计数、再删除，确认驱动与集合可用。
 *
 * <p>路径在 {@code /social/**} 下，网关已有转发规则，JWT 拦截器同样生效。
 *
 * @author yang
 * @since 2026-10-24
 */
@RestController
@RequestMapping("/social/chat")
@RequiredArgsConstructor
public class ChatPingController {

    private final MongoTemplate mongoTemplate;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * @return Mongo 状态、当前文档数与数据库名
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(0L);
        msg.setFromUserId(0L);
        msg.setMsgType("TEXT");
        msg.setContent("ping");
        msg.setCreatedAt(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(msg);
        long count = chatMessageRepository.count();
        chatMessageRepository.delete(saved);
        return Map.of("mongo", "ok", "count", count, "db", mongoTemplate.getDb().getName());
    }
}