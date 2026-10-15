package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.service.AuthService;
import com.campus.food.campusfood.util.JwtUtil;
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
    /** 与 DDL 默认一致：性别未知、动态公开、允许加好友 */
    private static final int GENDER_UNKNOWN = 0;
    private static final int PRIVACY_YES = 1;
    private static final int PWD_MIN_LEN = 6;
    private static final int PWD_MAX_LEN = 32;
    private static final int NICKNAME_MAX_LEN = 20;
    private static final String LOGIN_FAILED_MSG = "账号或密码错误";

    @Autowired
    private UserMapper sysUserMapper;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    /** 校验、查重、BCrypt 入库，默认 roleId=1 */
    @Override
    public RegisterVO register(RegisterDTO dto) {
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

        if (phone != null && sysUserMapper.countByPhone(phone) > 0) {
            throw new BizException("该手机号已被注册");
        }
        if (email != null && sysUserMapper.countByEmail(email) > 0) {
            throw new BizException("该邮箱已被注册");
        }

        String passwordHash = encoder.encode(password);

        User user = new User();
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setNickname(nickname);
        user.setGender(GENDER_UNKNOWN);
        user.setPrivacyFeed(PRIVACY_YES);
        user.setPrivacyAddFriend(PRIVACY_YES);
        user.setRoleId(ROLE_USER);
        user.setStatus(STATUS_NORMAL);
        user.setDeleted(NOT_DELETED);

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

    /** 校验密码与账号状态，签发 JWT */
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
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getRoleId()));
        return vo;
    }

    /** 含 {@code @} 按邮箱查，否则按手机号查 */
    private User findUserByAccount(String account) {
        if (account.contains("@")) {
            return sysUserMapper.selectByEmail(account);
        }
        return sysUserMapper.selectByPhone(account);
    }

    /** 去首尾空白，空串视为 null */
    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
