package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 移出成员成功体。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealRemoveResultVO {

    private Long activityId;

    private Long userId;

    private Integer currentMembers;
}
