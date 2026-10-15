package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码登录请求体，对应 {@code POST /auth/login}。
 */
@Data
public class LoginDTO {

    /** 登录账号：手机号或邮箱 
    * username 表示手机号或邮箱，由前端传入
    */
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
