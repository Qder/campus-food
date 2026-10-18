package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.service.FoodItemService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FoodItemServiceImpl implements FoodItemService {
    private final FoodItemMapper foodItemMapper;

    public FoodItemServiceImpl(FoodItemMapper foodItemMapper) {
        this.foodItemMapper = foodItemMapper;
    }

    /**
         * 按商户ID查询美食。
         *
         * @param merchantId 商户ID
         * @param offset     起始偏移量
         * @param size       每页条数
         * @return 美食列表；无数据返回空列表
         */
    @Override
    public List<FoodItem> selectByMerchantId(Long merchantId, int offset, int size) {
        return foodItemMapper.selectByMerchantId(merchantId, offset, size);
    }
}
