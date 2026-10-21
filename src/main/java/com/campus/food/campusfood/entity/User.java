package com.campus.food.campusfood.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统用户，对应表 {@code sys_user}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** 用户 ID */
    private Long id;

    /** 手机号，与邮箱至少填一个 */
    private String phone;

    /** 邮箱，与手机号至少填一个 */
    private String email;

    /** BCrypt 等密码哈希，列 {@code password_hash} */
    private String passwordHash;

    /** 昵称 */
    private String nickname;
    
    /** 头像 URL */
    private String avatarUrl;

    /** 性别，见 {@link com.campus.food.campusfood.enums.UserGenderEnum} */
    private Integer gender;

    /** 口味标签，逗号分隔，供推荐打分 */
    private String tasteTags;

    /** 忌口食材，逗号分隔，推荐时过滤 */
    private String dislikeIngredients;

    /** 学校 */
    private String school;

    /** 专业 */
    private String major;

    /** 个人简介 */
    private String bio;

    /** 动态是否公开：0 否，1 是 */
    private Integer privacyFeed;

    /** 是否允许陌生人加好友：0 否，1 是 */
    private Integer privacyAddFriend;

    /** 状态：0 禁用，1 正常，2 限制 */
    private Integer status;

    /**
     * 当前角色 ID，对应 {@code sys_role.id}。
     * 种子：1=USER，2=MERCHANT，3=ADMIN；注册未指定时默认为 USER(1)。
     */
    private Integer roleId;

    /** 最近登录时间 */
    private LocalDateTime lastLoginAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除：0 否，1 是 */
    private Integer deleted;
}