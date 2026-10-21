package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 规则推荐的单条结果。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class FoodRecommendVO {

    /** 命中的口味标签，逗号分隔 */
    private String recommendTags;

    /** 推荐分，区间 0~1 */
    private double score;

    /** 被推荐的美食 */
    private FoodItemVO food;
}
