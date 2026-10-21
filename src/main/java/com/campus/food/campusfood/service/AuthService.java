package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;

/**
 * 注册与登录。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface AuthService {

    /**
     * 注册新用户。手机号与邮箱至少填一个，默认角色为普通用户。
     *
     * @param registerDto 注册信息，不能为空
     * @return 新建用户的公开字段，不含密码
     * @throws com.campus.food.campusfood.exception.BizException 账号已注册或字段不合法时
     */
    RegisterVO register(RegisterDTO registerDto);

    /**
     * 使用手机号或邮箱登录，成功后签发 JWT。
     *
     * @param loginDto 账号与明文密码，不能为空
     * @return 用户公开字段及 JWT
     * @throws com.campus.food.campusfood.exception.BizException 账号或密码错误、账号状态异常时
     */
    LoginVO login(LoginDTO loginDto);
}
