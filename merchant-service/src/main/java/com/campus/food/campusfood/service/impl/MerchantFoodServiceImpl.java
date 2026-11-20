package com.campus.food.campusfood.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CreateReplyDTO;
import com.campus.food.campusfood.dto.MerchantFoodSaveDTO;
import com.campus.food.campusfood.entity.FoodItem;
import com.campus.food.campusfood.entity.Merchant;
import com.campus.food.campusfood.entity.ReviewImage;
import com.campus.food.campusfood.entity.ReviewReply;
import com.campus.food.campusfood.enums.FoodItemStatus;
import com.campus.food.campusfood.enums.MerchantAuditStatus;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.FoodReviewMapper;
import com.campus.food.campusfood.mapper.MerchantMapper;
import com.campus.food.campusfood.mapper.ReviewImageMapper;
import com.campus.food.campusfood.mapper.ReviewReplyMapper;
import com.campus.food.campusfood.service.MerchantFoodService;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;

/**
 * 店主菜品维护实现。新增只挂到当前用户第一家审核通过的店；评价查询须通过 {@link #requireOwnedFood(Long)} 防越权。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class MerchantFoodServiceImpl implements MerchantFoodService {

    /** 默认页码 */
    private static final int DEFAULT_PAGE = 1;
    /** 默认每页条数 */
    private static final int DEFAULT_SIZE = 10;
    /** 每页条数上限 */
    private static final int MAX_SIZE = 50;
    /** 回复内容长度上限，与 review_reply.content 列一致 */
    private static final int REPLY_MAX_LENGTH = 1000;
    /** 回复来源：商家 */
    private static final int FROM_MERCHANT = 1;
    /** 课设：写入后直接展示，跳过待审 */
    private static final int AUDIT_PASS = 1;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private FoodReviewMapper foodReviewMapper;

    @Autowired
    private ReviewReplyMapper reviewReplyMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

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
    public PageVO<FoodItemVO> listMine(Integer page, Integer size, Integer status, Integer categoryId,
                                       Long merchantId) {
        Long curUserId = requireMerchantUserId();
        validateOwnedMerchantId(merchantId, curUserId);

        int p = page == null || page < 1 ? DEFAULT_PAGE : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        int total = foodItemMapper.countByOwnerUserId(curUserId, merchantId, status, categoryId);
        List<FoodItemVO> list = total == 0
                ? List.of()
                : foodItemMapper.selectByOwnerUserId(curUserId, merchantId, status, categoryId, (p - 1) * s, s);

        PageVO<FoodItemVO> vo = new PageVO<>();
        vo.setTotal(total);
        vo.setPage(p);
        vo.setSize(s);
        vo.setList(list);
        return vo;
    }

    /** 若指定店铺 ID，须属于当前店主。 */
    private void validateOwnedMerchantId(Long merchantId, Long curUserId) {
        if (merchantId == null) {
            return;
        }
        Merchant shop = merchantMapper.selectById(merchantId);
        if (shop == null || !curUserId.equals(shop.getOwnerUserId())) {
            throw new BizException("您没有权限操作");
        }
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
        vo.setCampusArea(merchant.getCampusArea());
        vo.setShopName(merchant.getShopName());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>与用户端 {@code GET /food/{id}/reviews} 使用同一套「可见」条件，区别在本方法强制校验店主身份。
     */
    @Override
    public PageVO<FoodReviewVO> listReviews(Long foodId, Integer page, Integer size) {
        requireOwnedFood(foodId);

        int p = page == null || page < 1 ? DEFAULT_PAGE : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        int total = foodReviewMapper.countVisibleByFoodId(foodId);
        List<FoodReviewVO> list = total == 0
                ? List.of()
                : foodReviewMapper.selectVisibleByFoodId(foodId, (p - 1) * s, s);

        PageVO<FoodReviewVO> vo = new PageVO<>();
        vo.setTotal(total);
        vo.setPage(p);
        vo.setSize(s);
        vo.setList(list);
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>先按 {@link #requireApprovedShop()} 与 {@link #requireOwnedFood(Long)} 校验店主身份，
     * 再用 {@code selectVisibleById} 校验评价可见且属于该菜品。同一评价已有商家回复时覆盖原行，
     * 不新增第二条；图片按评价 ID 回表，避免响应里缺图。
     */
    @Override
    @Transactional
    public FoodReviewVO replyToReview(Long foodId, Long reviewId, CreateReplyDTO dto) {
        Long curUserId = requireMerchantUserId();
        requireApprovedShop();
        requireOwnedFood(foodId);

        if (reviewId == null) {
            throw new BizException("评价不存在");
        }
        FoodReviewVO review = foodReviewMapper.selectVisibleById(reviewId);
        if (review == null || !foodId.equals(review.getFoodId())) {
            throw new BizException("评价不存在");
        }

        String content = dto == null || dto.getContent() == null ? null : dto.getContent().trim();
        if (content == null || content.isEmpty()) {
            throw new BizException("回复内容不能为空");
        }
        if (content.length() > REPLY_MAX_LENGTH) {
            throw new BizException("回复内容过长");
        }

        LocalDateTime now = LocalDateTime.now();
        ReviewReply existing = reviewReplyMapper.selectMerchantReplyByReviewId(reviewId);
        if (existing != null) {
            reviewReplyMapper.updateContentById(existing.getId(), content, now);
        } else {
            ReviewReply reply = new ReviewReply();
            reply.setReviewId(reviewId);
            reply.setUserId(curUserId);
            reply.setContent(content);
            reply.setFromMerchant(FROM_MERCHANT);
            reply.setAuditStatus(AUDIT_PASS);
            reply.setCreatedAt(now);
            reviewReplyMapper.insert(reply);
        }

        review.setMerchantReply(content);
        review.setMerchantReplyAt(now);
        List<String> images = new ArrayList<>();
        for (ReviewImage image : reviewImageMapper.selectByReviewIds(List.of(reviewId))) {
            images.add(image.getUrl());
        }
        review.setImages(images);
        return review;
    }

    /** {@inheritDoc} */
    @Override
    public FoodItemVO getFoodDetail(Long id) {
        FoodItem foodItem = requireOwnedFoodDetail(id);
        return toFoodItemVo(foodItem, null);
    }

    /** 美食存在且属于当前用户的店。 */
    private FoodItem requireOwnedFoodDetail(Long id) {
        if (id == null) {
            throw new BizException("美食 ID 不能为空");
        }
        Long curUserId = requireMerchantUserId();
        FoodItem foodItem = foodItemMapper.selectById(id);
        if (foodItem == null) {
            throw new BizException("美食不存在");
        }
        if (foodItem == null || !curUserId.equals(foodItem.getMerchantId())) {
            throw new BizException("您没有权限操作");
        }
        return foodItem;
    }
}
