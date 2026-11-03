package com.campus.food.campusfood.dto;

import java.time.OffsetDateTime;

import lombok.Data;

/**
 * 发起拼单入参，对应 {@code POST /social/meal-activities}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class CreateMealActivityDTO {

    /** 活动名称，1–128 字 */
    private String title;

    /** 活动时间，必须晚于当前；契约为带时区 ISO-8601 */
    private OffsetDateTime meetTime;

    /** 见面地点，1–255 字 */
    private String meetPlace;

    /** 美食类型，可空，≤ 64 字 */
    private String foodType;

    /** 关联美食 ID，可空 */
    private Long relatedFoodId;

    /** 关联店铺 ID，可空 */
    private Long relatedMerchantId;

    /** 人数上限，含发起人，2–50 */
    private Integer maxMembers;

    /** 说明，可空，≤ 1000 字 */
    private String description;

    /** 报名截止，可空；有则必须早于 meetTime */
    private OffsetDateTime signupDeadline;
}
