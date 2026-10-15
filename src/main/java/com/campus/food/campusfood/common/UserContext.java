package com.campus.food.campusfood.common;

/**
 * 当前 HTTP 请求对应的登录用户，由 JWT 拦截器写入。
 */
public final class UserContext {
    //从threadlocal中获取当前登录用户的id和角色id
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE_ID = new ThreadLocal<>();

    private UserContext() {
    }

    /** 绑定当前请求的用户 ID 与角色 ID */
    public static void set(Long userId, Integer roleId) {
        USER_ID.set(userId);
        ROLE_ID.set(roleId);
    }

    /** 获取当前用户 ID，未登录时为 {@code null} */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前角色 ID，未登录或未写入时为 {@code null}。
     * 供后续按角色鉴权（商家 / 管理端）使用，当前业务尚未调用。
     */
    @SuppressWarnings("unused")
    public static Integer getRoleId() {
        return ROLE_ID.get();
    }

    /** 请求结束时清理，避免线程池复用导致串号 */
    public static void clear() {
        USER_ID.remove();
        ROLE_ID.remove();
    }
}
