package com.campus.food.campusfood.service.recommend;

import com.campus.food.campusfood.dto.FoodRatingPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户-菜品隐式评分表。邻居只用评价矩阵；当前用户向量再叠浏览和收藏。
 *
 * @author yang
 * @since 2026-11-08
 */
public final class UserItemMatrix {

    private UserItemMatrix() {
    }

    /**
     * 用可见评价建矩阵：用户 →（菜品 → 0~1 分）。同一人同一菜多次评价取更高分。
     *
     * @param pairs 评价三元组，可为 null
     * @return 评价矩阵；无数据返回空 Map
     */
    public static Map<Long, Map<Long, Double>> fromReviews(List<FoodRatingPair> pairs) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        if (pairs == null || pairs.isEmpty()) {
            return matrix;
        }
        for (FoodRatingPair pair : pairs) {
            if (pair == null || pair.getUserId() == null || pair.getFoodId() == null) {
                continue;
            }
            double value = RecommendSignals.reviewToImplicit(pair.getScore());
            if (value <= 0) {
                continue;
            }
            putMax(matrix, pair.getUserId(), pair.getFoodId(), value);
        }
        return matrix;
    }

    /**
     * 当前用户向量：评价 ∪ 浏览 ∪ 收藏，同菜取 max。
     *
     * @param userId        当前用户
     * @param reviewMatrix  全站评价矩阵
     * @param browsedFoodIds 浏览过的美食 ID，可为 null
     * @param favoriteFoodIds 收藏的美食 ID，可为 null
     * @return 菜品 → 隐式分；没有任何行为时为空 Map
     */
    public static Map<Long, Double> currentUserVector(Long userId,
                                                      Map<Long, Map<Long, Double>> reviewMatrix,
                                                      List<Long> browsedFoodIds,
                                                      List<Long> favoriteFoodIds) {
        Map<Long, Double> vector = new HashMap<>();
        if (userId != null && reviewMatrix != null) {
            Map<Long, Double> reviews = reviewMatrix.get(userId);
            if (reviews != null) {
                vector.putAll(reviews);
            }
        }
        mergeConstant(vector, browsedFoodIds, RecommendSignals.BROWSE);
        mergeConstant(vector, favoriteFoodIds, RecommendSignals.FAVORITE);
        return vector;
    }

    /**
     * 当前用户已经写过评价的菜，推荐时不再主推。
     *
     * @param userId       当前用户
     * @param reviewMatrix 评价矩阵
     * @return 已评价美食 ID；没有则空 Map 的 keySet 调用方自己建 Set
     */
    public static Map<Long, Double> reviewsOf(Long userId, Map<Long, Map<Long, Double>> reviewMatrix) {
        if (userId == null || reviewMatrix == null) {
            return Map.of();
        }
        Map<Long, Double> row = reviewMatrix.get(userId);
        if (row == null || row.isEmpty()) {
            return Map.of();
        }
        return row;
    }

    private static void mergeConstant(Map<Long, Double> vector, List<Long> foodIds, double weight) {
        if (foodIds == null || foodIds.isEmpty()) {
            return;
        }
        for (Long foodId : foodIds) {
            if (foodId == null) {
                continue;
            }
            Double old = vector.get(foodId);
            if (old == null || weight > old) {
                vector.put(foodId, weight);
            }
        }
    }

    private static void putMax(Map<Long, Map<Long, Double>> matrix, Long userId, Long foodId, double value) {
        Map<Long, Double> row = matrix.get(userId);
        if (row == null) {
            row = new HashMap<>();
            matrix.put(userId, row);
        }
        Double old = row.get(foodId);
        if (old == null || value > old) {
            row.put(foodId, value);
        }
    }
}
