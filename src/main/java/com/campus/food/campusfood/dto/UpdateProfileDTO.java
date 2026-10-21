package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新当前用户资料，对应 {@code PUT /user/profile}。未传的字段不修改。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class UpdateProfileDTO {

    /** 昵称，最长 20 字 */
    @Size(max = 20, message = "昵称长度不能超过 20 位")
    private String nickname;

    /** 性别：0 未知，1 男，2 女 */
    @Min(value = 0, message = "性别取值无效")
    @Max(value = 2, message = "性别取值无效")
    private Integer gender;

    /** 头像 URL */
    @Size(max = 512, message = "头像 URL 过长")
    private String avatarUrl;

    /** 学校 */
    @Size(max = 128, message = "学校名称过长")
    private String school;

    /** 专业 */
    @Size(max = 128, message = "专业名称过长")
    private String major;

    /** 口味标签，逗号分隔 */
    @Size(max = 256, message = "口味标签过长")
    private String tasteTags;

    /** 忌口食材，逗号分隔 */
    @Size(max = 256, message = "不喜欢的食材过长")
    private String dislikeIngredients;

    /** 个人简介 */
    @Size(max = 512, message = "个人简介过长")
    private String bio;

    /** 动态是否公开：0 否，1 是 */
    @Min(value = 0, message = "privacyFeed 取值无效")
    @Max(value = 1, message = "privacyFeed 取值无效")
    private Integer privacyFeed;

    /** 是否允许陌生人加好友：0 否，1 是 */
    @Min(value = 0, message = "privacyAddFriend 取值无效")
    @Max(value = 1, message = "privacyAddFriend 取值无效")
    private Integer privacyAddFriend;
}
