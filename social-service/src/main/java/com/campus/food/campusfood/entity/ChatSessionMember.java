package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data 
public class ChatSessionMember {
    private Long id;
    private Long sessionId;
    private Long userId;
    private Long unreadCount;
    private LocalDateTime joinedAt;
}
