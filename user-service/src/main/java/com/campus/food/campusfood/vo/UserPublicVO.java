package com.campus.food.campusfood.vo;

import lombok.Data;

@Data
public class UserPublicVO {

    private Long id;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private String school;

    private String major;

    private Integer privacyFeed;
}
