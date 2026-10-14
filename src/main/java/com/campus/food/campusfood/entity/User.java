package com.campus.food.campusfood.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统用户，对应表 {@code sys_user}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** 用户 ID */
    private Long id;

    private String phone;

    private String email;

    /** BCrypt 等密码哈希，列 {@code password_hash} */
    private String passwordHash;

    /** 昵称 */
    private String nickname;
    
    /** 头像 URL */
    private String avatarUrl;

    /** {@link UserGender} */
    private Integer gender;

    private String school;

    private String major;

    /** 个人简介 */
    private String bio;

    /** 动态是否公开：0 否，1 是 */
    private Integer privacyFeed;

    /** 是否允许陌生人加好友：0 否，1 是 */
    private Integer privacyAddFriend;

    /** {@link UserStatus} */
    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 逻辑删除：0 否，1 是 */
    private Integer deleted;
}
