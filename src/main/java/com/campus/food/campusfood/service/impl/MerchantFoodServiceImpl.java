package com.campus.food.campusfood.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.enums.FoodItemStatus;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.service.MerchantFoodService;
import com.campus.food.campusfood.vo.FoodItemVO;

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

    /** {@inheritDoc} */
    @Override
    public List<FoodItemVO> listMine() {
        Long curUserId = requireMerchantUserId();
        List<Merchant> merchants = merchantMapper.selectByOwnerUserId(curUserId);
        if (merchants == null || merchants.isEmpty()) {
            return List.of();
        }
        List<FoodItemVO> result = new ArrayList<>();
        for (Merchant merchant : merchants) {
            List<FoodItem> foodItems = foodItemMapper.selectByMerchantId(merchant.getId(), 0, 1000);
            if (foodItems == null || foodItems.isEmpty()) {
                continue;
            }
            for (FoodItem item : foodItems) {
                result.add(toFoodItemVo(item, merchant));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void onShelf(Long id) {
        FoodItem foodItem = requireOwnedFood(id);
        foodItem.setStatus(FoodItemStatus.ON_SHELF.getCode());
        foodItemMapper.updateById(foodItem);
    }

    /** {@inheritDoc} */
    @Override
    public void offShelf(Long id) {
        FoodItem foodItem = requireOwnedFood(id);
        foodItem.setStatus(FoodItemStatus.OFF_SHELF.getCode());
        foodItemMapper.updateById(foodItem);
    }

    /**
     * 将 {@link FoodItem} 转为 {@link FoodItemVO}。店铺名、校区来自 {@code merchant}，为 null 时不填。
     *
     * @param item     菜品实体，为 null 时返回 null
     * @param merchant 所属店铺，可为 null
     * @return 展示用 VO
     */
    private FoodItemVO toFoodItemVo(FoodItem item, Merchant merchant) {
        if (item == null) {
            return null;
        }
        FoodItemVO vo = new FoodItemVO();
        vo.setId(item.getId());
        vo.setMerchantId(item.getMerchantId());
        if (merchant != null) {
            vo.setShopName(merchant.getShopName());
            vo.setCampusArea(merchant.getCampusArea());
        }
        vo.setCategoryId(item.getCategoryId());
        vo.setName(item.getName());
        vo.setFoodType(item.getFoodType());
        vo.setPrice(item.getPrice());
        vo.setStock(item.getStock());
        vo.setDescription(item.getDescription());
        vo.setCoverUrl(item.getCoverUrl());
        vo.setAvgScore(item.getAvgScore());
        vo.setReviewCount(item.getReviewCount());
        vo.setSalesCount(item.getSalesCount());
        vo.setRecommendTag(item.getRecommendTag());
        vo.setStatus(item.getStatus());
        vo.setTasteTags(item.getTasteTags());
        vo.setIngredients(item.getIngredients());
        return vo;
    }
}
