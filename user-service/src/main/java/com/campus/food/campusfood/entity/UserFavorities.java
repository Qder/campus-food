package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserFavorities {

    private Long id;
    private Long userId;
    private Long targetId;
    private Integer targetType;
    private LocalDateTime createdAt;
}
