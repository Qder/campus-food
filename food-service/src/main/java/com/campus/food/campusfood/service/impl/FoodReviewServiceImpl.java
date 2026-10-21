package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CreateReviewDTO;
import com.campus.food.campusfood.entity.FoodReview;
import com.campus.food.campusfood.entity.User;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.FoodReviewMapper;
import com.campus.food.campusfood.mapper.UserMapper;
import com.campus.food.campusfood.service.FoodReviewService;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 美食评价服务实现。发表评价时同步回写美食的评价数与均分。
 *
 * @author yang
 * @since 2026-10-16
 */
@Service
public class FoodReviewServiceImpl implements FoodReviewService {

    /** 默认页码 */
    private static final int DEFAULT_PAGE = 1;
    /** 默认每页条数 */
    private static final int DEFAULT_SIZE = 10;
    /** 每页条数上限 */
    private static final int MAX_SIZE = 50;
    /** 课设：发表后直接展示，跳过待审 */
    private static final int AUDIT_PASS = 1;
    /** 评价状态：展示中 */
    private static final int STATUS_SHOW = 1;

    @Autowired
    private FoodReviewMapper foodReviewMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private UserMapper userMapper;

    /** {@inheritDoc} */
    @Override
    public PageVO<FoodReviewVO> listByFoodId(Long foodId, Integer page, Integer size) {
        requireOnShelfFood(foodId);

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
     * <p>入库评价与美食均分回写在同一事务内，保证两者一致。
     */
    @Override
    @Transactional
    public FoodReviewVO create(Long foodId, CreateReviewDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }

        FoodItemVO food = requireOnShelfFood(foodId);

        String content = trimToNull(dto.getContent());

        FoodReview review = new FoodReview();
        review.setUserId(userId);
        review.setFoodId(foodId);
        review.setMerchantId(food.getMerchantId());
        review.setScore(dto.getScore());
        review.setContent(content);
        review.setLikeCount(0);
        review.setAuditStatus(AUDIT_PASS);
        review.setStatus(STATUS_SHOW);

        foodReviewMapper.insert(review);
        foodItemMapper.updateScoreAfterReview(foodId, dto.getScore());

        // 返回列表同构字段，便于前端直接展示
        User user = userMapper.selectById(userId);
        FoodReviewVO vo = new FoodReviewVO();
        vo.setId(review.getId());
        vo.setUserId(userId);
        vo.setNickname(user != null ? user.getNickname() : null);
        vo.setFoodId(foodId);
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setLikeCount(0);
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }

    /**
     * 校验美食存在且已上架，仅这类美食允许看/写评价。
     *
     * @param foodId 美食 ID
     * @return 上架美食详情
     * @throws BizException 美食 ID 为空、不存在或未上架时
     */
    private FoodItemVO requireOnShelfFood(Long foodId) {
        if (foodId == null) {
            throw new BizException("美食不存在");
        }
        FoodItemVO food = foodItemMapper.selectOnShelfById(foodId);
        if (food == null) {
            throw new BizException("美食不存在或未上架");
        }
        return food;
    }

    /**
     * 去首尾空白。
     *
     * @param s 原始字符串，可为 null
     * @return 去空白后的字符串；空串返回 null
     */
    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
