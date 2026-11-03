package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.CancelMealActivityDTO;
import com.campus.food.campusfood.dto.CreateMealActivityDTO;
import com.campus.food.campusfood.dto.SubmitMealFeedbackDTO;
import com.campus.food.campusfood.vo.MealActivityCreateVO;
import com.campus.food.campusfood.vo.MealActivityMemberVO;
import com.campus.food.campusfood.vo.MealActivityVO;
import com.campus.food.campusfood.vo.MealCancelResultVO;
import com.campus.food.campusfood.vo.MealFeedbackCreateVO;
import com.campus.food.campusfood.vo.MealFeedbackVO;
import com.campus.food.campusfood.vo.MealJoinResultVO;
import com.campus.food.campusfood.vo.MealRemoveResultVO;
import com.campus.food.campusfood.vo.PageVO;

/**
 * 拼单约饭：发布、列表、报名、名单、取消、结束后反馈。
 *
 * <p>挂在 social-service，表是 {@code meal_activity*}，不是 {@code food_order}。
 *
 * @author yang
 * @since 2026-11-02
 */
public interface MealActivityService {

    /**
     * 拼单列表。未登录也可看；{@code mine}/{@code joined} 为 true 时必须已登录。
     *
     * @param status   活动状态，可为 null
     * @param foodType 美食类型，可为 null
     * @param mine     true 只看我发起的
     * @param joined   true 只看我报名的
     * @param page     页码，从 1
     * @param size     每页条数
     * @return 分页
     */
    PageVO<MealActivityVO> list(Integer status, String foodType, Boolean mine, Boolean joined,
                                Integer page, Integer size);

    /**
     * 发起拼单。当前用户自动占 1 人。
     *
     * @param dto 活动信息
     * @return 活动 ID、报名中、人数 1
     */
    MealActivityCreateVO create(CreateMealActivityDTO dto);

    /**
     * 拼单详情。未登录也可看；已取消当不存在。
     *
     * @param activityId 活动 ID
     * @return 详情，含已报名成员
     */
    MealActivityVO getDetail(Long activityId);

    /**
     * 报名参加。不能报自己的；须报名中、未满、未截止。
     *
     * @param activityId 活动 ID
     * @return 报名后人数与状态
     */
    MealJoinResultVO join(Long activityId);

    /**
     * 普通成员退出。发起人请走取消。
     *
     * @param activityId 活动 ID
     * @return 退出后人数
     */
    MealJoinResultVO quit(Long activityId);

    /**
     * 管理名单：含退出和移除。仅发起人。
     *
     * @param activityId 活动 ID
     * @return 成员列表
     */
    List<MealActivityMemberVO> listMembers(Long activityId);

    /**
     * 发起人移出一名成员。不能移自己。
     *
     * @param activityId 活动 ID
     * @param userId     被移出的用户
     * @return 活动 ID、被移出用户、当前人数
     */
    MealRemoveResultVO removeMember(Long activityId, Long userId);

    /**
     * 发起人取消活动。
     *
     * @param activityId 活动 ID
     * @param dto        原因可选，本期不落库
     * @return 活动 ID 与状态 4
     */
    MealCancelResultVO cancel(Long activityId, CancelMealActivityDTO dto);

    /**
     * 结束后提交反馈。仅已报名成员。
     *
     * @param activityId 活动 ID
     * @param dto        评分与可选对象
     * @return 反馈 ID
     */
    MealFeedbackCreateVO submitFeedback(Long activityId, SubmitMealFeedbackDTO dto);

    /**
     * 该活动的反馈列表。仅已报名成员可看。
     *
     * @param activityId 活动 ID
     * @return 反馈；没有返回空列表
     */
    List<MealFeedbackVO> listFeedback(Long activityId);
}
