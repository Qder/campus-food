package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.MealActivityFeedback;

/**
 * {@code meal_activity_feedback} Mapper。
 *
 * @author yang
 * @since 2026-11-02
 */
@Mapper
public interface MealActivityFeedbackMapper {

    /**
     * 插入一条反馈，回填 {@code id}。
     *
     * @param row 须带 activityId、fromUserId、score
     * @return 影响行数
     */
    int insert(MealActivityFeedback row);

    /**
     * 该活动全部反馈，按时间升序。
     *
     * @param activityId 活动 ID
     * @return 反馈列表；没有返回空列表
     */
    List<MealActivityFeedback> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 同一人对同一对象（{@code toUserId} 空表示活动整体）是否已评过。
     *
     * @param activityId 活动 ID
     * @param fromUserId 评价人
     * @param toUserId   被评价人，可为 null
     * @return 已有条数
     */
    int countByFromTo(@Param("activityId") Long activityId,
                      @Param("fromUserId") Long fromUserId,
                      @Param("toUserId") Long toUserId);
}
