package com.campus.food.campusfood.service.recommend;

import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.RecommendFavoriteMapper;
import com.campus.food.campusfood.service.util.FoodRecommendScorer;
import com.campus.food.campusfood.util.TagTextUtil;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodRecommendVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 推荐管道：协同过滤 + MLP，冷启动回退口味规则或热门销量。
 *
 * @author yang
 * @since 2026-11-08
 */
@Service
public class RecommendService {

    private static final double CF_WEIGHT = 0.6;
    private static final double RULE_WEIGHT = 0.4;

    private final FoodItemMapper foodItemMapper;
    private final RecommendFavoriteMapper recommendFavoriteMapper;
    private final UserFeignClient userFeignClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final RecommendModelStore recommendModelStore;

    /**
     * @param foodItemMapper           候选池
     * @param recommendFavoriteMapper  当前用户收藏
     * @param userFeignClient          口味与忌口
     * @param stringRedisTemplate      浏览历史
     * @param recommendModelStore      评价矩阵与 MLP
     */
    public RecommendService(FoodItemMapper foodItemMapper,
                            RecommendFavoriteMapper recommendFavoriteMapper,
                            UserFeignClient userFeignClient,
                            StringRedisTemplate stringRedisTemplate,
                            RecommendModelStore recommendModelStore) {
        this.foodItemMapper = foodItemMapper;
        this.recommendFavoriteMapper = recommendFavoriteMapper;
        this.userFeignClient = userFeignClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.recommendModelStore = recommendModelStore;
    }

    /**
     * 对上架候选打分并截断 TopN。须登录；无口味不报错。
     *
     * @param userId 当前用户
     * @param topN   返回条数
     * @param cap    候选池上限
     * @return 按推荐分倒序
     */
    public List<FoodRecommendVO> recommend(Long userId, int topN, int cap) {
        List<FoodItemVO> candidates = foodItemMapper.selectOnShelfForRecommend(cap);
        if (candidates.isEmpty()) {
            return List.of();
        }

        int maxSales = 1;
        Map<Long, FoodItemVO> itemsById = new HashMap<>();
        Set<Long> candidateIds = new HashSet<>();
        for (FoodItemVO row : candidates) {
            itemsById.put(row.getId(), row);
            candidateIds.add(row.getId());
            if (row.getSalesCount() != null && row.getSalesCount() > maxSales) {
                maxSales = row.getSalesCount();
            }
        }

        UserInfo user = userFeignClient.getUserInfo(userId);
        List<String> userTastes = user == null
                ? List.of()
                : TagTextUtil.parseTags(user.getTasteTags());
        Set<String> dislikes = user == null
                ? Set.of()
                : TagTextUtil.parseTagSet(user.getDislikeIngredients());

        RecommendModelStore.Snapshot model = recommendModelStore.get(itemsById, maxSales);
        Map<Long, Double> userVector = UserItemMatrix.currentUserVector(
                userId,
                model.reviewMatrix(),
                loadBrowseFoodIds(userId),
                loadFavoriteFoodIds(userId));
        Set<Long> alreadyRated = new HashSet<>(UserItemMatrix.reviewsOf(userId, model.reviewMatrix()).keySet());
        Map<Long, Double> cfScores = UserBasedCollaborativeFilter.predict(
                userId, userVector, model.reviewMatrix(), candidateIds, alreadyRated);
        RecommendMlp mlp = model.mlp();
        boolean mlpReady = mlp != null && mlp.isReady();

        List<FoodRecommendVO> scored = new ArrayList<>();
        for (FoodItemVO row : candidates) {
            if (TagTextUtil.containsAnyTag(TagTextUtil.parseTagSet(row.getIngredients()), dislikes)) {
                continue;
            }
            FoodItem food = toScoringItem(row);
            double tagHit = userTastes.isEmpty() ? 0 : tagHitRate(userTastes, food);
            double avgNorm = FoodRecommendScorer.normalizedAvgScore(row.getAvgScore());
            double salesNorm = FoodRecommendScorer.normalizedSales(row.getSalesCount(), maxSales);
            double ruleScore;
            if (userTastes.isEmpty()) {
                ruleScore = salesNorm;
            } else {
                ruleScore = FoodRecommendScorer.scorePersonalized(food, userTastes, maxSales);
            }
            Double cf = cfScores.get(row.getId());
            double cfScore = cf == null ? 0 : cf;
            double raw;
            if (mlpReady) {
                raw = mlp.predict(userId, row.getId(), RecommendMlp.extraOf(cfScore, tagHit, avgNorm, salesNorm));
            } else if (cf != null) {
                raw = CF_WEIGHT * cf + RULE_WEIGHT * ruleScore;
            } else {
                raw = ruleScore;
            }

            FoodRecommendVO vo = new FoodRecommendVO();
            vo.setFood(stripRecommendOnlyFields(row));
            vo.setScore(FoodRecommendScorer.roundScore(raw));
            vo.setRecommendTags(userTastes.isEmpty()
                    ? null
                    : TagTextUtil.joinTags(FoodRecommendScorer.matchedRecommendTags(userTastes, food)));
            scored.add(vo);
        }

        scored.sort(Comparator
                .comparing(FoodRecommendVO::getScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(v -> v.getFood().getSalesCount(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(v -> v.getFood().getId(), Comparator.nullsLast(Comparator.reverseOrder())));
        if (scored.size() <= topN) {
            return scored;
        }
        return new ArrayList<>(scored.subList(0, topN));
    }

    private List<Long> loadBrowseFoodIds(Long userId) {
        List<Long> ids = new ArrayList<>();
        try {
            List<String> raw = stringRedisTemplate.opsForList()
                    .range(RecommendSignals.browseKey(userId), 0, -1);
            if (raw == null) {
                return ids;
            }
            for (String s : raw) {
                if (s == null || s.isBlank()) {
                    continue;
                }
                try {
                    ids.add(Long.valueOf(s.trim()));
                } catch (NumberFormatException ignored) {
                    // 脏数据跳过
                }
            }
        } catch (Exception ignored) {
            // Redis 不可用时只靠评价和收藏
        }
        return ids;
    }

    private List<Long> loadFavoriteFoodIds(Long userId) {
        try {
            List<Long> ids = recommendFavoriteMapper.selectFoodIdsByUser(userId);
            return ids == null ? List.of() : ids;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private static double tagHitRate(List<String> userTastes, FoodItem food) {
        List<String> hits = FoodRecommendScorer.matchedRecommendTags(userTastes, food);
        if (userTastes.isEmpty()) {
            return 0;
        }
        return (double) hits.size() / userTastes.size();
    }

    private static FoodItem toScoringItem(FoodItemVO row) {
        FoodItem item = new FoodItem();
        item.setId(row.getId());
        item.setFoodType(row.getFoodType());
        item.setTasteTags(row.getTasteTags());
        item.setIngredients(row.getIngredients());
        item.setAvgScore(row.getAvgScore());
        item.setSalesCount(row.getSalesCount());
        return item;
    }

    private static FoodItemVO stripRecommendOnlyFields(FoodItemVO row) {
        FoodItemVO out = new FoodItemVO();
        out.setId(row.getId());
        out.setMerchantId(row.getMerchantId());
        out.setShopName(row.getShopName());
        out.setCampusArea(row.getCampusArea());
        out.setCategoryId(row.getCategoryId());
        out.setName(row.getName());
        out.setFoodType(row.getFoodType());
        out.setPrice(row.getPrice());
        out.setStock(row.getStock());
        out.setDescription(row.getDescription());
        out.setCoverUrl(row.getCoverUrl());
        out.setAvgScore(row.getAvgScore());
        out.setReviewCount(row.getReviewCount());
        out.setSalesCount(row.getSalesCount());
        out.setRecommendTag(row.getRecommendTag());
        out.setStatus(row.getStatus());
        return out;
    }
}
