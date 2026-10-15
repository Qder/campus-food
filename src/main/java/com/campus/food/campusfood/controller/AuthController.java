package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.service.AuthService;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** 用户注册，无需 Token */
    @PostMapping("/register")
    public RegisterVO register(@Valid @RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }

    /** 密码登录，返回 JWT */
    @PostMapping("/login")
    public LoginVO login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }
}
