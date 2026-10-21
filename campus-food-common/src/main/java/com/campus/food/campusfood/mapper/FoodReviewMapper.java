package com.campus.food.campusfood.mapper;

import com.campus.food.campusfood.entity.FoodReview;
import com.campus.food.campusfood.vo.FoodReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * {@code food_review} MyBatis Mapper
 *
 * @author yang
 * @since 2026-10-16
 */
@Mapper
public interface FoodReviewMapper {

    /**
     * 新增评价。
     *
     * @param entity 待插入的评价
     * @return 影响行数
     */
    int insert(FoodReview entity);

    /**
     * 统计某美食下可见的评价数。
     *
     * @param foodId 美食 ID
     * @return 可见评价条数
     */
    int countVisibleByFoodId(@Param("foodId") Long foodId);

    /**
     * 分页查询某美食下可见的评价。
     *
     * @param foodId 美食 ID
     * @param offset 起始偏移量
     * @param size   每页条数
     * @return 可见评价列表；无数据返回空列表
     */
    List<FoodReviewVO> selectVisibleByFoodId(@Param("foodId") Long foodId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);
}
