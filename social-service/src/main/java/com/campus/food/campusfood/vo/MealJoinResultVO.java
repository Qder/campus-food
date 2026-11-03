package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 报名 / 退出成功体。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealJoinResultVO {

    private Long activityId;

    private Boolean joined;

    private Integer currentMembers;

    /** 报名成功才带；退出不填 */
    private Integer status;
}
