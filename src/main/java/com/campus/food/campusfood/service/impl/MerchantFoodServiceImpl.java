package com.campus.food.campusfood.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.service.MerchantFoodService;

/**
 * 店主菜品维护实现。新增只挂到当前用户第一家审核通过的店。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class MerchantFoodServiceImpl implements MerchantFoodService {

    @Autowired
    private  FoodItemMapper foodItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * @param foodItemMapper 菜品 Mapper
     */
    public MerchantFoodServiceImpl(FoodItemMapper foodItemMapper) {
        this.foodItemMapper = foodItemMapper;
    }

    /** {@inheritDoc} */
    @Override
    public Long save(MerchantFoodSaveDTO dto) {
        Merchant merchant = requireApprovedShop();
        FoodItem foodItem = toFoodItem(dto);
        foodItem.setMerchantId(merchant.getId());
        foodItem.setStatus(1);
        foodItem.setDeleted(0);
        foodItem.setId(null);
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

    /** {@inheritDoc} */
    @Override
    public void deleteById(Long id) {
        FoodItem foodItem = requireOwnedFood(id);
        foodItemMapper.logicalDeleteById(foodItem.getId());
    }

    /** {@inheritDoc} */
    @Override
    public int updateById(Long id, MerchantFoodSaveDTO dto) {
        FoodItem foodItem = requireOwnedFood(id);
        foodItem.setName(dto.getName());
        foodItem.setPrice(dto.getPrice());
        foodItem.setStock(dto.getStock());
        foodItem.setDescription(dto.getDescription());
        foodItem.setCoverUrl(dto.getCoverUrl());
        foodItem.setTasteTags(dto.getTasteTags());
        foodItem.setCategoryId(dto.getCategoryId());
        foodItem.setIngredients(dto.getIngredients());
        return foodItemMapper.updateById(foodItem);
    }

    /** 当前用户第一家审核通过的店，供新增绑 merchantId。 */
    private Merchant requireApprovedShop() {
        Long curUserId = requireMerchantUserId();
        List<Merchant> merchants = merchantMapper.selectByOwnerUserId(curUserId);
        if (merchants == null || merchants.isEmpty()) {
            throw new BizException("您没有店铺");
        }
        for (Merchant merchant : merchants) {
            if (Integer.valueOf(MerchantAuditStatus.APPROVED.getCode()).equals(merchant.getAuditStatus())) {
                return merchant;
            }
        }
        throw new BizException("您的店铺未审核通过");
    }

    /** 菜存在且属于当前用户的店。 */
    private FoodItem requireOwnedFood(Long id) {
        if (id == null) {
            throw new BizException("美食 ID 不能为空");
        }
        Long curUserId = requireMerchantUserId();
        FoodItem foodItem = foodItemMapper.selectById(id);
        if (foodItem == null) {
            throw new BizException("美食不存在");
        }
        Merchant shop = merchantMapper.selectById(foodItem.getMerchantId());
        if (shop == null || !curUserId.equals(shop.getOwnerUserId())) {
            throw new BizException("您没有权限操作");
        }
        return foodItem;
    }

    /** 取当前登录用户。未登录时中断，避免菜品挂到空店主上。 */
    private Long requireMerchantUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        return userId;
    }
}
