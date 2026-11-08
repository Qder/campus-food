package com.campus.food.campusfood.service.recommend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于用户的协同过滤：余弦找 TopK 邻居，再对候选菜做加权平均。
 *
 * <p>邻居向量只用评价矩阵，不扫全站浏览 Redis。
 *
 * @author yang
 * @since 2026-11-08
 */
public final class UserBasedCollaborativeFilter {

    /** 邻居人数上限 */
    public static final int TOP_K = 10;

    private UserBasedCollaborativeFilter() {
    }

    /**
     * 预测当前用户对候选菜的 CF 分（0~1）。没有邻居或邻居没吃过的菜不会出现在结果里。
     *
     * @param userId         当前用户
     * @param userVector     当前用户向量（评价 ∪ 浏览 ∪ 收藏）
     * @param reviewMatrix   全站评价矩阵（邻居只用这个）
     * @param candidateIds   候选美食 ID
     * @param alreadyRated   当前用户已评价的菜，这些不出现在结果里
     * @return 菜品 → CF 分；冷启动返回空 Map
     */
    public static Map<Long, Double> predict(Long userId,
                                            Map<Long, Double> userVector,
                                            Map<Long, Map<Long, Double>> reviewMatrix,
                                            Set<Long> candidateIds,
                                            Set<Long> alreadyRated) {
        Map<Long, Double> result = new HashMap<>();
        if (userId == null || userVector == null || userVector.isEmpty()
                || reviewMatrix == null || reviewMatrix.isEmpty()
                || candidateIds == null || candidateIds.isEmpty()) {
            return result;
        }

        List<Neighbor> neighbors = topNeighbors(userId, userVector, reviewMatrix);
        if (neighbors.isEmpty()) {
            return result;
        }

        for (Long foodId : candidateIds) {
            if (foodId == null) {
                continue;
            }
            if (alreadyRated != null && alreadyRated.contains(foodId)) {
                continue;
            }
            double weighted = 0;
            double simSum = 0;
            for (Neighbor neighbor : neighbors) {
                Map<Long, Double> row = reviewMatrix.get(neighbor.userId);
                if (row == null) {
                    continue;
                }
                Double rating = row.get(foodId);
                if (rating == null) {
                    continue;
                }
                weighted += neighbor.similarity * rating;
                simSum += neighbor.similarity;
            }
            if (simSum <= 0) {
                continue;
            }
            result.put(foodId, weighted / simSum);
        }
        return result;
    }

    /**
     * 与其他有评价用户算余弦，取相似度大于 0 的 TopK。
     *
     * @param userId       当前用户，不与自己比
     * @param userVector   当前用户向量
     * @param reviewMatrix 邻居评价矩阵
     * @return 按相似度从高到低，最多 {@link #TOP_K} 个
     */
    static List<Neighbor> topNeighbors(Long userId,
                                       Map<Long, Double> userVector,
                                       Map<Long, Map<Long, Double>> reviewMatrix) {
        List<Neighbor> all = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : reviewMatrix.entrySet()) {
            Long otherId = entry.getKey();
            if (otherId == null || otherId.equals(userId)) {
                continue;
            }
            Map<Long, Double> other = entry.getValue();
            if (other == null || other.isEmpty()) {
                continue;
            }
            double sim = cosine(userVector, other);
            if (sim <= 0) {
                continue;
            }
            all.add(new Neighbor(otherId, sim));
        }
        all.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        if (all.size() <= TOP_K) {
            return all;
        }
        return new ArrayList<>(all.subList(0, TOP_K));
    }

    /**
     * 余弦相似度。分母为 0 或无交集时返回 0。
     *
     * @param a 用户向量
     * @param b 邻居评价向量
     * @return 0~1 附近的相似度，无交集为 0
     */
    static double cosine(Map<Long, Double> a, Map<Long, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (Double v : a.values()) {
            if (v != null) {
                normA += v * v;
            }
        }
        for (Map.Entry<Long, Double> e : b.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            double bv = e.getValue();
            normB += bv * bv;
            Double av = a.get(e.getKey());
            if (av != null) {
                dot += av * bv;
            }
        }
        if (dot <= 0 || normA <= 0 || normB <= 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 一个相似用户。
     *
     * @param userId     邻居 ID
     * @param similarity 与当前用户的余弦相似度
     */
    record Neighbor(Long userId, double similarity) {
    }
}
