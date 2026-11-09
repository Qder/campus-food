package com.campus.food.campusfood.service.recommend;

import com.campus.food.campusfood.dto.FoodRatingPair;
import com.campus.food.campusfood.mapper.FoodReviewMapper;
import com.campus.food.campusfood.vo.FoodItemVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评价矩阵与 MLP 的短缓存，避免每次推荐全表扫描和重训。
 *
 * @author yang
 * @since 2026-11-08
 */
@Component
public class RecommendModelStore {

    /** 缓存有效期，毫秒 */
    private static final long TTL_MS = 2 * 60 * 1000L;

    private final FoodReviewMapper foodReviewMapper;
    private final Object lock = new Object();
    private volatile Snapshot snapshot;

    /**
     * @param foodReviewMapper 可见评价
     */
    public RecommendModelStore(FoodReviewMapper foodReviewMapper) {
        this.foodReviewMapper = foodReviewMapper;
    }

    /**
     * 取当前快照；过期则重新拉评价并训练 MLP。
     *
     * @param items    当前候选菜，训练时取均分/销量
     * @param maxSales 销量归一化分母
     * @return 评价矩阵与 MLP
     */
    public Snapshot get(Map<Long, FoodItemVO> items, int maxSales) {
        long now = System.currentTimeMillis();
        Snapshot cached = snapshot;
        if (cached != null && now - cached.createdAtMs < TTL_MS) {
            return cached;
        }
        synchronized (lock) {
            cached = snapshot;
            if (cached != null && now - cached.createdAtMs < TTL_MS) {
                return cached;
            }
            List<FoodRatingPair> pairs = foodReviewMapper.selectRatingPairs();
            if (pairs == null) {
                pairs = List.of();
            }
            Map<Long, Map<Long, Double>> reviewMatrix = UserItemMatrix.fromReviews(pairs);
            RecommendMlp mlp = new RecommendMlp();
            mlp.train(pairs, items == null ? Map.of() : items, maxSales);
            Snapshot fresh = new Snapshot(reviewMatrix, new ArrayList<>(pairs), mlp, now);
            snapshot = fresh;
            return fresh;
        }
    }

    /**
     * 一次加载的评价矩阵与模型。
     *
     * @param reviewMatrix 用户 → 菜品 → 隐式分
     * @param pairs        原始评价对
     * @param mlp          已尝试训练的网络
     * @param createdAtMs  生成时间
     */
    public record Snapshot(Map<Long, Map<Long, Double>> reviewMatrix,
                           List<FoodRatingPair> pairs,
                           RecommendMlp mlp,
                           long createdAtMs) {
    }
}
