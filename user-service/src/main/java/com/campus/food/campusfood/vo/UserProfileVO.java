package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 当前用户资料视图，不含密码等敏感字段。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class UserProfileVO {

    /** 用户 ID */
    private Long id;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    /** 性别，取值见 {@link com.campus.food.campusfood.enums.UserGenderEnum} */
    private Integer gender;

    /** 学校名称 */
    private String school;

    /** 专业名称 */
    private String major;

    /** 个人简介 */
    private String bio;

    /** 动态是否公开：0 否，1 是 */
    private Integer privacyFeed;

    /** 是否允许陌生人加好友：0 否，1 是 */
    private Integer privacyAddFriend;

    /** 角色 ID */
    private Integer roleId;

    /** 账号状态：0 禁用，1 正常 */
    private Integer status;

    /** 喜欢的口味，逗号分隔 */
    private String tasteTags;

    /** 不喜欢的食材，逗号分隔 */
    private String dislikeIngredients;
}
