package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FriendRequest {
    
    Long id;
    Long fromUserId;
    Long toUserId;
    String message;
    Integer status;
    LocalDateTime createdAt;
    LocalDateTime handledAt;
}
