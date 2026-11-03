package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 发起拼单成功体。发起人已占 1 人。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealActivityCreateVO {

    private Long activityId;

    private Integer status;

    private Integer currentMembers;

    private LocalDateTime createdAt;
}
