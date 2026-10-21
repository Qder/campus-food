package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.service.FoodItemService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * {@link FoodItemService} 的实现，查询委托给 Mapper。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class FoodItemServiceImpl implements FoodItemService {
    private final FoodItemMapper foodItemMapper;

    /**
     * @param foodItemMapper 菜品 Mapper
     */
    public FoodItemServiceImpl(FoodItemMapper foodItemMapper) {
        this.foodItemMapper = foodItemMapper;
    }

    /** {@inheritDoc} */
    @Override
    public List<FoodItem> selectByMerchantId(Long merchantId, int offset, int size) {
        return foodItemMapper.selectByMerchantId(merchantId, offset, size);
    }
}
