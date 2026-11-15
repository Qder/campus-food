package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.ReviewImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * {@code review_image} MyBatis Mapper
 *
 * @author yang
 * @since 2026-11-15
 */
@Mapper
public interface ReviewImageMapper {

    /**
     * 批量新增评价图片，调用方需保证列表非空。
     *
     * @param list 待插入的图片，含 reviewId、url、sortNo
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ReviewImage> list);

    /**
     * 按评价 ID 集合一次性查询图片，避免逐条查询。
     *
     * @param reviewIds 评价 ID 集合，不可为空
     * @return 图片列表，按评价 ID 与排序号升序；无数据返回空列表
     */
    List<ReviewImage> selectByReviewIds(@Param("reviewIds") List<Long> reviewIds);
}