package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FriendRequestVo {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private Integer status;
    private LocalDateTime createAt;
}
