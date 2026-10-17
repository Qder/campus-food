package com.campus.food.campusfood.aspect;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.exception.BizException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminOnlyAspect {

    /** 与 sys_role 种子 ADMIN 一致 */
    private static final int ROLE_ADMIN = 3;

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
