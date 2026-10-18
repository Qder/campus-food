package com.campus.food.campusfood.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.service.MerchantFoodService;


@Service
public class MerchantFoodServiceImpl implements MerchantFoodService {

    @Autowired
    private  FoodItemMapper foodItemMapper;

    public MerchantFoodServiceImpl(FoodItemMapper foodItemMapper) {
        this.foodItemMapper = foodItemMapper;
    }

    @Override
    public Long save(MerchantFoodSaveDTO dto) {
        FoodItem foodItem = new FoodItem();
        foodItem = toFoodItem(dto);
        foodItemMapper.insert(foodItem);
        return foodItem.getId();
    }
    
    private FoodItem toFoodItem(MerchantFoodSaveDTO dto) {
        FoodItem foodItem = new FoodItem();
        foodItem.setId(dto.getId());
        foodItem.setCategoryId(dto.getCategoryId());
        foodItem.setName(dto.getName());
        foodItem.setPrice(dto.getPrice());
        foodItem.setStock(dto.getStock());
        foodItem.setDescription(dto.getDescription());
        foodItem.setCoverUrl(dto.getCoverUrl());
        foodItem.setTasteTags(dto.getTasteTags());
        foodItem.setIngredients(dto.getIngredients());
        return foodItem;
    }

    @Override
    public void deleteById(Long id) {
        foodItemMapper.logicalDeleteById(id);
    }

    @Override
    public int updateById(Long id, MerchantFoodSaveDTO dto) {
        FoodItem foodItem = foodItemMapper.selectById(id);
        if (foodItem == null) {
            throw new RuntimeException("美食不存在");
        }
        foodItem = toFoodItem(dto);
        foodItem.setId(id);
        return foodItemMapper.updateById(foodItem);
    }
}
