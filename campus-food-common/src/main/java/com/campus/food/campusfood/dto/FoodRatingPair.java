package com.campus.food.campusfood.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可见评价的用户-菜品-评分三元组，供协同过滤与 MLP 训练。
 *
 * @author yang
 * @since 2026-11-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodRatingPair {

    /** 评价人 */
    private Long userId;

    /** 美食 */
    private Long foodId;

    /** 评分 1–5 */
    private Integer score;
}
