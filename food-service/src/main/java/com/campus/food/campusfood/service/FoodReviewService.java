package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.CreateReviewDTO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;

/**
 * 美食评价服务：评价分页与发表。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface FoodReviewService {

    /**
     * 可见评价分页（审核通过且展示中）。
     *
     * @param foodId 美食 ID
     * @param page   页码，可为 null，默认 1
     * @param size   每页条数，可为 null，默认 10，最大 50
     * @return 评价分页结果
     * @throws com.campus.food.campusfood.exception.BizException 美食不存在或未上架时
     */
    PageVO<FoodReviewVO> listByFoodId(Long foodId, Integer page, Integer size);

    /**
     * 当前登录用户发表评价，课设默认直接通过审核。
     *
     * @param foodId 美食 ID
     * @param dto    评价内容
     * @return 新建的评价
     * @throws com.campus.food.campusfood.exception.BizException 未登录或美食不可评价时
     */
    FoodReviewVO create(Long foodId, CreateReviewDTO dto);
}
