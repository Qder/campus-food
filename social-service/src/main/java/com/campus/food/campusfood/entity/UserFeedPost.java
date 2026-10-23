package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserFeedPost {
    Long id;
    Long userId;
    Long foodId;
    String content;
    LocalDateTime createdAt;
    String imageUrls;
}
