package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatSession {
    private Long id;
    private String sessionType;
    private Long peerUserId;
    private Long groupId;
    private String lastMsgPreview;
    private LocalDateTime lastMsgAt;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
