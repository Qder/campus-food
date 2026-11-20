package com.campus.food.campusfood.service.impl;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.dto.CreateReplyDTO;
import com.campus.food.campusfood.dto.CreateReviewDTO;
import com.campus.food.campusfood.entity.FoodReview;
import com.campus.food.campusfood.entity.ReviewImage;
import com.campus.food.campusfood.entity.ReviewLike;
import com.campus.food.campusfood.entity.ReviewReply;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.FoodItemMapper;
import com.campus.food.campusfood.mapper.FoodReviewMapper;
import com.campus.food.campusfood.mapper.ReviewImageMapper;
import com.campus.food.campusfood.mapper.ReviewLikeMapper;
import com.campus.food.campusfood.mapper.ReviewReplyMapper;
import com.campus.food.campusfood.service.FoodReviewService;
import com.campus.food.campusfood.vo.FoodItemVO;
import com.campus.food.campusfood.vo.FoodReviewVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.ReviewLikeVO;
import com.campus.food.campusfood.vo.ReviewReplyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 美食评价服务实现。发表评价时同步回写美食的评价数与均分，并支持评价带图、点赞与回复。
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
    /** 图片地址长度上限，与 review_image.url 列一致 */
    private static final int IMAGE_URL_MAX_LENGTH = 512;
    /** 回复内容长度上限，与 review_reply.content 列一致 */
    private static final int REPLY_MAX_LENGTH = 1000;
    /** 回复来源：普通用户 */
    private static final int FROM_USER = 0;

    @Autowired
    private FoodReviewMapper foodReviewMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    @Autowired
    private ReviewLikeMapper reviewLikeMapper;

    @Autowired
    private ReviewReplyMapper reviewReplyMapper;

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
        fillImages(list);

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
     * <p>入库评价、评价图片与美食均分回写在同一事务内，保证三者一致。
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

        List<String> images = validateImages(dto.getImages());
        insertImages(review.getId(), images);

        foodItemMapper.updateScoreAfterReview(foodId, dto.getScore());

        // 返回列表同构字段，便于前端直接展示
        UserInfo user = userFeignClient.getUserInfo(userId);
        FoodReviewVO vo = new FoodReviewVO();
        vo.setId(review.getId());
        vo.setUserId(userId);
        vo.setNickname(user != null ? user.getNickname() : null);
        vo.setFoodId(foodId);
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setLikeCount(0);
        vo.setImages(images);
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>先删后插：删除命中说明此前已点赞，取消点赞并将点赞数减一；
     * 未删除则新增点赞并加一。并发重复插入命中唯一键时按已点赞处理，不再累加。
     */
    @Override
    @Transactional
    public ReviewLikeVO toggleLike(Long reviewId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        // 确认该评论是否是可见的，如果不可见直接返回400
        requireVisibleReview(reviewId);

        boolean liked;
        // 先删后插：删除命中说明此前已点赞，取消点赞并将点赞数减一；
        if (reviewLikeMapper.deleteByReviewAndUser(reviewId, userId) > 0) {
            foodReviewMapper.incrementLikeCount(reviewId, -1);
            liked = false;
        } else {
            ReviewLike like = new ReviewLike();
            like.setReviewId(reviewId);
            like.setUserId(userId);
            try {
                reviewLikeMapper.insert(like);
                foodReviewMapper.incrementLikeCount(reviewId, 1);
                liked = true;
            } catch (DuplicateKeyException e) {
                // 并发下同一用户已插入点赞，按已点赞返回，点赞数不重复累加
                liked = true;
            }
        }

        FoodReviewVO review = foodReviewMapper.selectVisibleById(reviewId);
        ReviewLikeVO vo = new ReviewLikeVO();
        vo.setReviewId(reviewId);
        vo.setLiked(liked);
        vo.setLikeCount(review != null ? review.getLikeCount() : 0);
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>昵称由用户服务提供，降级时返回 null，不影响回复落库。
     */
    @Override
    @Transactional
    public ReviewReplyVO addReply(Long reviewId, CreateReplyDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        requireVisibleReview(reviewId);

        String content = trimToNull(dto.getContent());
        if (content == null) {
            throw new BizException("回复内容不能为空");
        }
        if (content.length() > REPLY_MAX_LENGTH) {
            throw new BizException("回复内容过长");
        }

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setFromMerchant(FROM_USER);
        reply.setAuditStatus(AUDIT_PASS);
        reply.setCreatedAt(LocalDateTime.now());
        reviewReplyMapper.insert(reply);

        UserInfo user = userFeignClient.getUserInfo(userId);
        ReviewReplyVO vo = new ReviewReplyVO();
        vo.setId(reply.getId());
        vo.setReviewId(reviewId);
        vo.setUserId(userId);
        vo.setNickname(user != null ? user.getNickname() : null);
        vo.setContent(reply.getContent());
        vo.setFromMerchant(reply.getFromMerchant());
        vo.setCreatedAt(reply.getCreatedAt());
        return vo;
    }

    /** {@inheritDoc} */
    @Override
    public List<ReviewReplyVO> listReplies(Long reviewId) {
        requireVisibleReview(reviewId);
        return reviewReplyMapper.selectByReviewId(reviewId);
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
     * 查询可见评价（审核通过且展示中），点赞与回复只允许这类评价。
     *
     * @param reviewId 评价 ID
     * @return 可见评价
     * @throws BizException 评价 ID 为空或评价不存在/不可见时
     */
    private FoodReviewVO requireVisibleReview(Long reviewId) {
        if (reviewId == null) {
            throw new BizException("评价不存在");
        }
        FoodReviewVO review = foodReviewMapper.selectVisibleById(reviewId);
        if (review == null) {
            throw new BizException("评价不存在");
        }
        return review;
    }

    /**
     * 校验图片地址并去除首尾空白。
     *
     * <p>只接受站内相对路径 {@code /uploads/} 或 http(s) 外链，防止写入非法地址。
     *
     * @param images 原始图片地址列表，可为 null
     * @return 校验后的地址列表；无图时返回空列表
     * @throws BizException 任一地址为空、超过 512 字符或前缀不合法时
     */
    private List<String> validateImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(images.size());
        for (String image : images) {
            String url = image == null ? "" : image.trim();
            if (url.isEmpty()
                    || url.length() > IMAGE_URL_MAX_LENGTH
                    || !(url.startsWith("/uploads/")
                    || url.startsWith("http://")
                    || url.startsWith("https://"))) {
                throw new BizException("图片地址不合法");
            }
            result.add(url);
        }
        return result;
    }

    /**
     * 批量写入评价图片，排序号取列表下标。
     *
     * @param reviewId 评价 ID
     * @param images   已校验的图片地址列表，可为空
     */
    private void insertImages(Long reviewId, List<String> images) {
        if (images.isEmpty()) {
            return;
        }
        List<ReviewImage> entities = new ArrayList<>(images.size());
        for (int i = 0; i < images.size(); i++) {
            ReviewImage image = new ReviewImage();
            image.setReviewId(reviewId);
            image.setUrl(images.get(i));
            image.setSortNo(i);
            entities.add(image);
        }
        reviewImageMapper.batchInsert(entities);
    }

    /**
     * 用当页评价 ID 一次性查询图片并按评价分组回填，避免逐条查询。
     *
     * @param reviews 当页评价，可为空；没有图片时回填空列表
     */
    private void fillImages(List<FoodReviewVO> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        List<Long> reviewIds = new ArrayList<>(reviews.size());
        for (FoodReviewVO review : reviews) {
            reviewIds.add(review.getId());
        }

        Map<Long, List<String>> imageMap = new HashMap<>();
        for (ReviewImage image : reviewImageMapper.selectByReviewIds(reviewIds)) {
            imageMap.computeIfAbsent(image.getReviewId(), key -> new ArrayList<>()).add(image.getUrl());
        }
        for (FoodReviewVO review : reviews) {
            review.setImages(imageMap.getOrDefault(review.getId(), new ArrayList<>()));
        }
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