package com.campus.food.campusfood.common;

/**
 * 当前 HTTP 请求对应的登录用户，由 JWT 拦截器写入，请求结束时清理。
 *
 * <p>用 {@link ThreadLocal} 把身份限制在当前请求线程，避免线程池复用时串用户。
 *
 * @author yang
 * @since 2026-10-16
 */
public final class UserContext {

    /** 当前请求的用户 ID */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    /** 当前请求的角色 ID */
    private static final ThreadLocal<Integer> ROLE_ID = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 绑定当前请求的用户 ID 与角色 ID。
     *
     * @param userId 用户 ID，可为 null
     * @param roleId 角色 ID，可为 null
     */
    public static void set(Long userId, Integer roleId) {
        USER_ID.set(userId);
        ROLE_ID.set(roleId);
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 用户 ID，未登录时为 {@code null}
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前角色 ID，供管理员鉴权等按角色判断使用。
     *
     * @return 角色 ID，未登录或令牌未写入时为 {@code null}
     */
    public static Integer getRoleId() {
        return ROLE_ID.get();
    }

    /**
     * 清理当前线程上的用户身份。必须在请求结束时调用，否则线程池复用会串号。
     */
    public static void clear() {
        USER_ID.remove();
        ROLE_ID.remove();
    }
}
