package com.campus.food.campusfood.aspect;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.exception.BizException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 拦截 {@link AdminOnly} 标记的接口，只放行平台管理员。
 *
 * @author yang
 * @since 2026-10-16
 */
@Aspect
@Component
public class AdminOnlyAspect {

    /** 平台管理员角色，与 {@code sys_role} 种子 ADMIN 一致 */
    private static final int ROLE_ADMIN = 3;

    /**
     * 进入控制器前核对当前用户角色。
     *
     * @throws BizException 未登录或角色不是管理员时
     */
    @Before("@within(com.campus.food.campusfood.aspect.AdminOnly) || @annotation(com.campus.food.campusfood.aspect.AdminOnly)")
    public void checkAdmin() {
        Integer roleId = UserContext.getRoleId();
        if (roleId == null) {
            throw new BizException("未登录");
        }
        if (roleId != ROLE_ADMIN) {
            throw new BizException("无管理员权限");
        }
    }
}
