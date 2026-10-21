package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.FoodItem;

import java.util.List;

/**
 * 按商户查询菜品。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface FoodItemService {

    /**
     * 按商户 ID 分页查询菜品。
     *
     * @param merchantId 商户 ID
     * @param offset     起始偏移量，从 0 起
     * @param size       每页条数
     * @return 菜品列表；无数据返回空列表
     */
    List<FoodItem> selectByMerchantId(Long merchantId, int offset, int size);
}
