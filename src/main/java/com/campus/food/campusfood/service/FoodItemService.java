package com.campus.food.campusfood.service;

import com.campus.food.campusfood.entity.FoodItem;
import java.util.List;

public interface FoodItemService {
    /**
         * 按商户ID查询美食。
         *
         * @param merchantId 商户ID
         * @param offset     起始偏移量
         * @param size       每页条数
         * @return 美食列表；无数据返回空列表
         */
    List<FoodItem> selectByMerchantId(Long merchantId, int offset, int size);
}
