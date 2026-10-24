package com.campus.food.campusfood.common;

/**
 * 社交模块 Redis key 前缀与拼接方法，集中一处避免各服务类各写一套。
 *
 * <p>两个集合成对使用：
 * <ul>
 *   <li>{@code social:follow:{userId}} —— Set，该用户关注（好友）的 ID</li>
 *   <li>{@code social:fans:{userId}}   —— Set，关注该用户的人 ID</li>
 * </ul>
 *
 * @author yang
 * @since 2026-10-24
 */
public final class SocialRedisKeys {

    /** 关注集合前缀 */
    private static final String FOLLOW_PREFIX = "social:follow:";

    /** 粉丝集合前缀 */
    private static final String FANS_PREFIX = "social:fans:";

    private SocialRedisKeys() {
    }

    /**
     * 关注集合 key。
     *
     * @param userId 用户 ID
     * @return 形如 {@code social:follow:1}
     */
    public static String follow(Long userId) {
        return FOLLOW_PREFIX + userId;
    }

    /**
     * 粉丝集合 key。
     *
     * @param userId 用户 ID
     * @return 形如 {@code social:fans:1}
     */
    public static String fans(Long userId) {
        return FANS_PREFIX + userId;
    }
}