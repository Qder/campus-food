package com.campus.food.campusfood.service.util;

import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.util.TagTextUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

/**
 * 课设规则推荐打分：标签 0.4 + 均分 0.3 + 销量 0.3（调用方保证用户口味非空）。
 *
 * @author yang
 * @since 2026-10-16
 */
public final class FoodRecommendScorer {

    /** 口味标签命中率权重 */
    private static final double WEIGHT_TAG = 0.4;
    /** 平均评分权重 */
    private static final double WEIGHT_SCORE = 0.3;
    /** 销量权重 */
    private static final double WEIGHT_SALES = 0.3;

    private FoodRecommendScorer() {
    }

    /**
     * 计算单个美食的个性化推荐分，结果落在 0~1。
     *
     * @param food          候选美食，需含口味/类型/均分/销量
     * @param userTastes    用户口味标签，非空
     * @param maxSalesInPool 候选池最高销量，用于销量归一化，至少为 1
     * @return 加权后的推荐分
     */
    public static double scorePersonalized(FoodItem food, List<String> userTastes, int maxSalesInPool) {
        double tagPart = tagHitRate(userTastes, food);
        double scorePart = normalizedAvgScore(food.getAvgScore());
        double salesPart = normalizedSales(food.getSalesCount(), maxSalesInPool);
        return WEIGHT_TAG * tagPart + WEIGHT_SCORE * scorePart + WEIGHT_SALES * salesPart;
    }

    /**
     * 求用户口味在美食标签（口味 + 类型）上的命中比例。
     *
     * @param userTastes 用户口味标签
     * @param food       候选美食
     * @return 命中数 / 用户口味数，区间 0~1
     */
    private static double tagHitRate(List<String> userTastes, FoodItem food) {
        if (userTastes.isEmpty()) {
            return 0;
        }
        Set<String> foodTags = TagTextUtil.mergeTagSets(food.getTasteTags(), food.getFoodType());
        int hits = TagTextUtil.matchedTags(userTastes, foodTags).size();
        return (double) hits / userTastes.size();
    }

    /**
     * 求用户口味在美食标签上的命中标签（保持用户标签顺序）。
     *
     * @param userTastes 用户口味标签
     * @param food       候选美食
     * @return 命中的标签列表；无命中返回空列表
     */
    public static List<String> matchedRecommendTags(List<String> userTastes, FoodItem food) {
        Set<String> foodTags = TagTextUtil.mergeTagSets(food.getTasteTags(), food.getFoodType());
        return TagTextUtil.matchedTags(userTastes, foodTags);
    }

    /**
     * 把平均分归一化到 0~1（以 5 分制为满分）。
     *
     * @param avgScore 平均分，可为 null
     * @return 归一化结果，null 或非正数按 0 处理
     */
    public static double normalizedAvgScore(BigDecimal avgScore) {
        if (avgScore == null || avgScore.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        double v = avgScore.doubleValue() / 5.0;
        return Math.min(1.0, Math.max(0, v));
    }

    /**
     * 把销量归一化到 0~1（以候选池最高销量为满分）。
     *
     * @param salesCount    销量，可为 null
     * @param maxSalesInPool 候选池最高销量，用作分母
     * @return 归一化结果，缺失销量按 0 处理
     */
    public static double normalizedSales(Integer salesCount, int maxSalesInPool) {
        int sales = salesCount == null ? 0 : salesCount;
        int cap = Math.max(maxSalesInPool, 1);
        return Math.min(1.0, (double) sales / cap);
    }

    /**
     * 推荐分保留 4 位小数，避免前端展示出现长浮点尾数。
     *
     * @param score 原始推荐分
     * @return 四舍五入到 4 位小数后的值
     */
    public static double roundScore(double score) {
        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
