package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.MealActivityMember;

/**
 * {@code meal_activity_member} Mapper。退出/移出改 status，不删行。
 *
 * @author yang
 * @since 2026-11-02
 */
@Mapper
public interface MealActivityMemberMapper {

    /**
     * 插入一行报名。发起时插发起人；报名时插新成员。
     *
     * @param row 须带 activityId、userId、role、status
     * @return 影响行数
     */
    int insert(MealActivityMember row);

    /**
     * 该用户在该活动的报名行，含已退出/已移除。
     *
     * @param activityId 活动 ID
     * @param userId     用户 ID
     * @return 成员行；没有返回 null
     */
    MealActivityMember selectByActivityAndUser(@Param("activityId") Long activityId,
                                               @Param("userId") Long userId);

    /**
     * 详情用：只返回 {@code status=1} 已报名。
     *
     * @param activityId 活动 ID
     * @return 已报名成员；没有返回空列表
     */
    List<MealActivityMember> selectJoinedByActivity(@Param("activityId") Long activityId);

    /**
     * 管理名单：该活动全部成员行，含退出和移除。
     *
     * @param activityId 活动 ID
     * @return 成员行，按报名时间升序
     */
    List<MealActivityMember> selectAllByActivity(@Param("activityId") Long activityId);

    /**
     * 改成员状态：报名中 / 退出 / 被移除。
     *
     * @param id     成员行 ID
     * @param status 目标状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
