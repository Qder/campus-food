package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.dto.LoginDTO;
import com.campus.food.campusfood.dto.RegisterDTO;
import com.campus.food.campusfood.dto.SendCodeDTO;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.service.AuthService;
import com.campus.food.campusfood.util.JwtUtil;
import com.campus.food.campusfood.vo.LoginVO;
import com.campus.food.campusfood.vo.RegisterVO;
import com.campus.food.campusfood.vo.SendCodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 注册与登录实现。密码只存 BCrypt，登录失败对外使用同一文案。
 *
 * @author yang
 * @since 2026-10-16
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** 普通用户角色，对应 {@code sys_role} 种子 USER */
    private static final int ROLE_USER = 1;
    /** 账号状态：1 正常；0 禁用，不能登录 */
    private static final int STATUS_NORMAL = 1;
    /** 未逻辑删除 */
    private static final int NOT_DELETED = 0;
    /** 性别未知，与表默认值一致 */
    private static final int GENDER_UNKNOWN = 0;
    /** 隐私开关打开：动态公开、允许加好友 */
    private static final int PRIVACY_YES = 1;
    /** 密码最短长度 */
    private static final int PWD_MIN_LEN = 6;
    /** 密码最长长度 */
    private static final int PWD_MAX_LEN = 32;
    /** 昵称最大长度 */
    private static final int NICKNAME_MAX_LEN = 20;
    /** 登录失败的统一文案，避免区分账号不存在和密码错误 */
    private static final String LOGIN_FAILED_MSG = "账号或密码错误";
    /** 验证码过期时间（秒） */
    private static final int CODE_EXPIRE_SECONDS = 300;
    /** 重发限制时间（秒） */
    private static final int RESEND_LIMIT_SECONDS = 60;
    /** 注册场景码，与 {@code /auth/code} 的 scene 一致 */
    private static final String SCENE_REGISTER = "REGISTER";

    @Autowired
    private UserMapper sysUserMapper;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * {@inheritDoc}
     *
     * <p>密码只存 BCrypt。唯一索引冲突转成业务错误，避免并发注册把数据库异常抛给调用方。
     */
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

        String targetType = phone != null ? "PHONE" : "EMAIL";
        String target = phone != null ? phone : email.toLowerCase(Locale.ROOT);
        String registerCodeKey = verifyCode(SCENE_REGISTER, targetType, target, dto.getCode());

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

        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BizException("手机号或邮箱已被注册");
        }
        // 注册成功后删除验证码，避免重复使用；删除失败不影响注册结果
        try {
            stringRedisTemplate.delete(registerCodeKey);
        } catch (Exception e) {
            log.warn("注册成功但删除验证码失败, key={}", registerCodeKey, e);
        }

        RegisterVO vo = new RegisterVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRoleId(user.getRoleId());
        return vo;
    }

    /**
     * 校验验证码。只读不删，删除由调用方在业务成功后执行。
     *
     * <p>场景、目标类型和目标共同定位 Redis 中的验证码；不存在、已过期或不匹配
     * 都抛同一文案，不向调用方区分原因。
     *
     * @param scene      场景，REGISTER / RESET
     * @param targetType 目标类型，PHONE / EMAIL
     * @param target     手机号，或已转小写的邮箱
     * @param code       请求提交的验证码
     * @return 验证码在 Redis 中的 key，供业务成功后删除
     * @throws BizException 验证码不存在、已过期或不匹配时
     */
    private String verifyCode(String scene, String targetType, String target, String code) {
        String submitted = code == null ? null : code.trim();
        String codeKey = String.format("auth:code:%s:%s:%s", scene, targetType, target);
        String expected = stringRedisTemplate.opsForValue().get(codeKey);
        if (expected == null || !expected.equals(submitted)) {
            throw new BizException("验证码错误或已过期");
        }
        return codeKey;
    }

    /**
     * {@inheritDoc}
     *
     * <p>账号不存在和密码错误返回同一文案。非正常状态单独提示，避免禁用账号反复试密码。
     */
    @Override
    public LoginVO login(LoginDTO loginDto) {
        String account = trimToNull(loginDto.getUsername());
        String password = loginDto.getPassword();

        if (account == null || password == null || password.isEmpty()) {
            throw new BizException(LOGIN_FAILED_MSG);
        }

        User user = findUserByAccount(account);
        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new BizException(LOGIN_FAILED_MSG);
        }
        if (user.getStatus() == null || user.getStatus() != STATUS_NORMAL) {
            throw new BizException("账号状态异常，无法登录");
        }

        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateLastLoginAt(user.getId(), user.getLastLoginAt());

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRoleId(user.getRoleId());
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getRoleId()));
        return vo;
    }

    @Override
    public SendCodeVO sendCode(SendCodeDTO dto) {
        String target = trimToNull(dto.getTarget());
        String type = trimToNull(dto.getTargetType());
        String scene = trimToNull(dto.getScene());

        if(target==null)
        {
            throw new BizException("target 不能为空");
        }

        if(!"PHONE".equals(type) && !"EMAIL".equals(type))
        {
            throw new BizException("targetType不合法");
        }

        if (!"REGISTER".equals(scene) && !"RESET".equals(scene)) {
            throw new BizException("scene 不合法");
        }

        if ("PHONE".equals(type)) {
            if (!target.matches("^1[3-9]\\d{9}$")) {
                throw new BizException("手机号格式不正确");
            }
        } else {
            if (!target.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new BizException("邮箱格式不正确");
            }
            target = target.toLowerCase();
        }

        //redis 限流，防止频繁发送验证码
        String limitKey = String.format("auth:code:limit:%s:%s:%s", scene, type, target);
        Boolean limitSuccess = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", RESEND_LIMIT_SECONDS, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(limitSuccess)) {
            throw new BizException("验证码发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        String codeKey = String.format("auth:code:%s:%s:%s", scene, type, target);
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("验证码 target={}, code={}", target, code);

        SendCodeVO vo = new SendCodeVO();
        vo.setExpireIn(CODE_EXPIRE_SECONDS);
        vo.setResendAfter(RESEND_LIMIT_SECONDS);
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
