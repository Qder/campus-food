package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 拼单报名一行，对应表 {@code meal_activity_member}。
 *
 * <p>唯一键 {@code (activity_id, user_id)}：退出/移出改 {@code status}，不删行。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealActivityMember {

    private Long id;

    private Long activityId;

    private Long userId;

    /** 0 成员 / 1 发起人 */
    private Integer role;

    /** 1 已报名 / 2 已退出 / 3 已被移除 */
    private Integer status;

    private LocalDateTime joinedAt;
}
