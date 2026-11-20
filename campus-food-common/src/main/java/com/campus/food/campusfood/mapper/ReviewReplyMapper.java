package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.ReviewReply;
import com.campus.food.campusfood.vo.ReviewReplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code review_reply} MyBatis Mapper
 *
 * @author yang
 * @since 2026-11-15
 */
@Mapper
public interface ReviewReplyMapper {

    /**
     * 新增回复。
     *
     * @param entity 待插入的回复
     * @return 影响行数
     */
    int insert(ReviewReply entity);

    /**
     * 查询某评价下审核通过的回复，带回复人昵称。
     *
     * @param reviewId 评价 ID
     * @return 按回复时间、ID 升序的回复列表；无数据返回空列表
     */
    List<ReviewReplyVO> selectByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 查询某评价下的商家回复，按 ID 倒序取一条。
     *
     * @param reviewId 评价 ID
     * @return 商家回复；没有返回 null
     */
    ReviewReply selectMerchantReplyByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 按主键覆盖回复内容并刷新回复时间，供商家重复回复同一评价时复用原行。
     *
     * @param id        回复 ID
     * @param content   新内容
     * @param createdAt 新的回复时间
     * @return 影响行数
     */
    int updateContentById(@Param("id") Long id,
                          @Param("content") String content,
                          @Param("createdAt") LocalDateTime createdAt);
}