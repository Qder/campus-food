package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;

public interface AuthService {

    RegisterVO register(RegisterDTO registerDto);

    LoginVO login(LoginDTO loginDto);
}
