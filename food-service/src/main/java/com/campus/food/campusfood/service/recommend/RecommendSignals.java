package com.campus.food.campusfood.service.recommend;

/**
 * 隐式反馈权重与 Redis 浏览 key。同菜多信号取 max。
 *
 * @author yang
 * @since 2026-11-08
 */
public final class RecommendSignals {

    /** 浏览一次折合的隐式分 */
    public static final double BROWSE = 0.3;

    /** 收藏美食折合的隐式分 */
    public static final double FAVORITE = 0.8;

    /** 详情写入的浏览 List 前缀，完整 key 为 {@code food:browse:{userId}} */
    public static final String BROWSE_KEY_PREFIX = "food:browse:";

    private RecommendSignals() {
    }

    /**
     * 把 1–5 分评价压到 0~1。
     *
     * @param score 评分，可为 null
     * @return 归一化结果；非法分按 0
     */
    public static double reviewToImplicit(Integer score) {
        if (score == null || score < 1) {
            return 0;
        }
        int capped = Math.min(score, 5);
        return capped / 5.0;
    }

    /**
     * 浏览历史 Redis key。
     *
     * @param userId 用户 ID
     * @return {@code food:browse:{userId}}
     */
    public static String browseKey(Long userId) {
        return BROWSE_KEY_PREFIX + userId;
    }
}
