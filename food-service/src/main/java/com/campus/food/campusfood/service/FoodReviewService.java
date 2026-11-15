package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.CreateReplyDTO;
import com.campus.food.campusfood.dto.CreateReviewDTO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.ReviewLikeVO;
import com.campus.food.campusfood.vo.ReviewReplyVO;

import java.util.List;

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

    /**
     * 点赞或取消点赞，仅对可见评价生效。
     *
     * @param reviewId 评价 ID
     * @return 切换后的点赞状态与最新点赞数
     * @throws com.campus.food.campusfood.exception.BizException 未登录或评价不存在/不可见时
     */
    ReviewLikeVO toggleLike(Long reviewId);

    /**
     * 当前登录用户回复评价。
     *
     * @param reviewId 评价 ID
     * @param dto      回复内容
     * @return 新建的回复
     * @throws com.campus.food.campusfood.exception.BizException 未登录、评价不存在/不可见或内容为空时
     */
    ReviewReplyVO addReply(Long reviewId, CreateReplyDTO dto);

    /**
     * 评价回复列表，按回复时间正序。
     *
     * @param reviewId 评价 ID
     * @return 回复列表；无回复返回空列表
     * @throws com.campus.food.campusfood.exception.BizException 评价不存在或不可见时
     */
    List<ReviewReplyVO> listReplies(Long reviewId);
}
