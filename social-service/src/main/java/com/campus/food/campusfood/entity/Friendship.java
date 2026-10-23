package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Friendship {
    Long id;
    Long userId;
    Long friendId;
    String remark;
    LocalDateTime createdAt;
}
