package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.service.AuthService;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {


    private static final int ROLE_USER = 1;
    private static final int STATUS_NORMAL = 1;
    private static final int NOT_DELETED = 0;
    private static final int PWD_MIN_LEN = 6;
    private static final int PWD_MAX_LEN = 32;
    private static final int NICKNAME_MAX_LEN = 20;
    private static final String LOGIN_FAILED_MSG = "账号或密码错误";

    @Autowired
    private UserMapper sysUserMapper;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Override
    public RegisterVO register(RegisterDTO dto) {
        // 1. 校验
        String phone = trimToNull(dto.getPhone());
        String email = trimToNull(dto.getEmail());
        String password = dto.getPassword();
        String nickname = trimToNull(dto.getNickname());

        if (phone == null && email == null) {
            throw new BizException("手机号或邮箱至少填写一个");
        }
        if (password == null || password.length() < PWD_MIN_LEN
                || password.length() > PWD_MAX_LEN) {
            throw new BizException("密码长度需在 " + PWD_MIN_LEN + "~" + PWD_MAX_LEN + " 位之间");
        }
        if (nickname == null) {
            throw new BizException("昵称不能为空");
        }
        if (nickname.length() > NICKNAME_MAX_LEN) {
            throw new BizException("昵称长度不能超过 " + NICKNAME_MAX_LEN + " 位");
        }

        // 2. 查重：同 phone / email 且未删除
        if (phone != null && sysUserMapper.countByPhone(phone) > 0) {
            throw new BizException("该手机号已被注册");
        }
        if (email != null && sysUserMapper.countByEmail(email) > 0) {
            throw new BizException("该邮箱已被注册");
        }

        // 3. 明文密码 -> BCrypt 哈希
        String passwordHash = encoder.encode(password);

        // 4. 组装实体：只设必须字段
        User user = new User();
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setNickname(nickname);
        user.setRoleId(ROLE_USER);
        user.setStatus(STATUS_NORMAL);
        user.setDeleted(NOT_DELETED);
        // privacy / bio / school 不设置，保持 NULL

        // 5. 插入；唯一索引冲突转友好错误
        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BizException("手机号或邮箱已被注册");
        }

        // 6. 返回安全字段
        RegisterVO vo = new RegisterVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRoleId(user.getRoleId());
        return vo;
    }

    @Override
    public LoginVO login(LoginDTO loginDto) {
        String account = trimToNull(loginDto.getUsername());
        String password = loginDto.getPassword();

        // 如果账号或密码为空，则抛出异常
        if (account == null || password == null || password.isEmpty()) {
            throw new BizException(LOGIN_FAILED_MSG);
        }

        // 根据账号查找用户
        User user = findUserByAccount(account);
        
        // 如果用户不存在或密码不匹配，则抛出异常
        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new BizException(LOGIN_FAILED_MSG);
        }
        // 如果用户状态异常，则抛出异常
        if (user.getStatus() == null || user.getStatus() != STATUS_NORMAL) {
            throw new BizException("账号状态异常，无法登录");
        }

        // 返回登录成功信息
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRoleId(user.getRoleId());
        return vo;
    }

    /** 账号为邮箱（含 @）或手机号 */
    private User findUserByAccount(String account) {
        if (account.contains("@")) {
            return sysUserMapper.selectByEmail(account);
        }
        return sysUserMapper.selectByPhone(account);
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
