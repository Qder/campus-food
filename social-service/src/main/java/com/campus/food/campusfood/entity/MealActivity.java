package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 拼单约饭活动，对应表 {@code meal_activity}。
 *
 * <p>约同学一起吃饭，不是购物车订单。成员在 {@code meal_activity_member}，
 * 结束后的评价在 {@code meal_activity_feedback}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealActivity {

    private Long id;

    /** 发起者用户 ID */
    private Long creatorId;

    /** 活动名称，1–128 字 */
    private String title;

    /** 活动时间，必须晚于发布时间 */
    private LocalDateTime meetTime;

    /** 见面地点 */
    private String meetPlace;

    /** 美食类型偏好，可空 */
    private String foodType;

    /** 关联 {@code food_item.id}，可空 */
    private Long relatedFoodId;

    /** 关联 {@code merchant.id}，可空 */
    private Long relatedMerchantId;

    /** 人数上限，含发起人，2–50 */
    private Integer maxMembers;

    /** 当前已报名人数，含发起人 */
    private Integer currentMembers;

    /** 说明，可空 */
    private String description;

    /** 报名截止，须早于 {@code meetTime}；可空表示不另设截止 */
    private LocalDateTime signupDeadline;

    /**
     * 0 报名中 / 1 已满 / 2 进行中 / 3 已结束 / 4 已取消。
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
