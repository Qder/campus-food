package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 拼单列表项 / 详情。详情多 {@code members}、{@code relatedFoodName}、{@code description}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class MealActivityVO {

    private Long activityId;

    private String title;

    private Long creatorId;

    private String creatorNickname;

    private LocalDateTime meetTime;

    private String meetPlace;

    private String foodType;

    private Long relatedFoodId;

    private String relatedFoodName;

    private Long relatedMerchantId;

    private Integer maxMembers;

    private Integer currentMembers;

    private String description;

    private LocalDateTime signupDeadline;

    private Integer status;

    /** 当前用户是否已报名；未登录为 false */
    private Boolean joined;

    /** 当前用户是否发起人；未登录为 false */
    private Boolean isCreator;

    /** 详情才填：status=1 的成员 */
    private List<MealActivityMemberVO> members;
}
