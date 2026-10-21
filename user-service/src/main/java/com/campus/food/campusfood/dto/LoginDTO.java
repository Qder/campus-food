package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码登录请求体，对应 {@code POST /auth/login}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class LoginDTO {

    /** 登录账号：手机号或邮箱 */
    @NotBlank(message = "账号不能为空")
    private String username;

    /** 明文密码，服务端用 BCrypt 比对，不落库 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
