package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 拼单成员一项。详情只带已报名；管理名单含退出/移除。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealActivityMemberVO {

    private Long userId;

    private String nickname;

    private String avatarUrl;

    /** 0 成员 / 1 发起人 */
    private Integer role;

    /** 1 已报名 / 2 已退出 / 3 已被移除 */
    private Integer status;

    /** 管理名单才带 */
    private LocalDateTime joinedAt;
}
