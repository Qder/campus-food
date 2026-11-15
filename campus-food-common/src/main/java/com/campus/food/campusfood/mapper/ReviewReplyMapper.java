package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.ReviewReply;
import com.campus.food.campusfood.vo.ReviewReplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}