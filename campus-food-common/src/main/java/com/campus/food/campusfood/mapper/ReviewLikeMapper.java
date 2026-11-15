package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.ReviewLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * {@code review_like} MyBatis Mapper
 *
 * @author yang
 * @since 2026-11-15
 */
@Mapper
public interface ReviewLikeMapper {

    /**
     * 新增点赞记录；同一用户重复点赞会命中唯一键 {@code uk_review_like}。
     *
     * @param entity 待插入的点赞记录
     * @return 影响行数
     */
    int insert(ReviewLike entity);

    /**
     * 删除指定用户对指定评价的点赞记录。
     *
     * @param reviewId 评价 ID
     * @param userId   用户 ID
     * @return 影响行数；0 表示此前未点赞
     */
    int deleteByReviewAndUser(@Param("reviewId") Long reviewId,
                              @Param("userId") Long userId);

    /**
     * 统计某评价的点赞记录数。
     *
     * @param reviewId 评价 ID
     * @return 点赞记录条数
     */
    int countByReviewId(@Param("reviewId") Long reviewId);
}