package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.AuthService;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册与登录，无需 JWT。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 注册新用户，默认角色为普通用户。
     *
     * @param dto 手机号或邮箱、密码、昵称
     * @return 新建用户的公开字段，不含密码
     * @throws BizException 账号已注册或字段不合法时
     */
    @PostMapping("/register")
    public RegisterVO register(@Valid @RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }

    /**
     * 使用手机号或邮箱登录。
     *
     * @param dto 账号与明文密码
     * @return 用户公开字段及 JWT
     * @throws BizException 账号或密码错误、账号状态异常时
     */
    @PostMapping("/login")
    public LoginVO login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }
}
