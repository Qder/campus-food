package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送验证码请求体，对应 {@code POST /auth/code}。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
public class SendCodeDTO {

    /** 手机号或邮箱 */
    @NotBlank(message = "target 不能为空")
    private String target;

    /** 目标类型：PHONE / EMAIL */
    @NotBlank(message = "targetType 不能为空")
    private String targetType;

    /** 场景：REGISTER / RESET */
    @NotBlank(message = "scene 不能为空")
    private String scene;
}