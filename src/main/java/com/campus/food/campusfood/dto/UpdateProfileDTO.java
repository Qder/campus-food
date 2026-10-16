package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新当前用户资料，对应 {@code PUT /user/profile}。未传的字段不修改。
 */
@Data
public class UpdateProfileDTO {

    @Size(max = 20, message = "昵称长度不能超过 20 位")
    private String nickname;

    @Min(value = 0, message = "性别取值无效")
    @Max(value = 2, message = "性别取值无效")
    private Integer gender;

    @Size(max = 512, message = "头像 URL 过长")
    private String avatarUrl;

    @Size(max = 128, message = "学校名称过长")
    private String school;

    @Size(max = 128, message = "专业名称过长")
    private String major;

    @Size(max = 256, message = "口味标签过长")
    private String tasteTags;

    @Size(max = 256, message = "不喜欢的食材过长")
    private String dislikeIngredients;

    @Size(max = 512, message = "个人简介过长")
    private String bio;

    @Min(value = 0, message = "privacyFeed 取值无效")
    @Max(value = 1, message = "privacyFeed 取值无效")
    private Integer privacyFeed;

    @Min(value = 0, message = "privacyAddFriend 取值无效")
    @Max(value = 1, message = "privacyAddFriend 取值无效")
    private Integer privacyAddFriend;
}
