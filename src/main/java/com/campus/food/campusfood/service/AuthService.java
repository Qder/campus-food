package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;

public interface AuthService {

    /** 注册新用户，默认角色 USER */
    RegisterVO register(RegisterDTO registerDto);

    /** 手机号或邮箱密码登录，成功返回 JWT */
    LoginVO login(LoginDTO loginDto);
}
