package com.campus.food.campusfood.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.CancelMealActivityDTO;
import com.campus.food.campusfood.dto.CreateMealActivityDTO;
import com.campus.food.campusfood.dto.SubmitMealFeedbackDTO;
import com.campus.food.campusfood.service.MealActivityService;
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
 * 拼单约饭，路径 {@code /social/meal-activities}。
 *
 * <p>挂在社交服务，不是订单服务。列表/详情可未登录；其余需 JWT。
 *
 * @author yang
 * @since 2026-11-02
 */
@RestController
@RequestMapping("/social/meal-activities")
public class MealActivityController {

    @Autowired
    private MealActivityService mealActivityService;

    /**
     * 拼单列表。未登录也可看；带 Token 才填 joined / isCreator。
     */
    @GetMapping
    public PageVO<MealActivityVO> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String foodType,
            @RequestParam(required = false) Boolean mine,
            @RequestParam(required = false) Boolean joined,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return mealActivityService.list(status, foodType, mine, joined, page, size);
    }

    /**
     * 发起拼单。当前用户自动占 1 人，状态报名中。
     */
    @PostMapping
    public MealActivityCreateVO create(@RequestBody CreateMealActivityDTO dto) {
        return mealActivityService.create(dto);
    }

    /**
     * 拼单详情。已取消当不存在。
     */
    @GetMapping("/{activityId}")
    public MealActivityVO getDetail(@PathVariable("activityId") Long activityId) {
        return mealActivityService.getDetail(activityId);
    }

    /**
     * 报名参加。不能报自己的；须未满且未截止。
     */
    @PostMapping("/{activityId}/join")
    public MealJoinResultVO join(@PathVariable("activityId") Long activityId) {
        return mealActivityService.join(activityId);
    }

    /**
     * 普通成员退出。发起人请走取消。
     */
    @PostMapping("/{activityId}/quit")
    public MealJoinResultVO quit(@PathVariable("activityId") Long activityId) {
        return mealActivityService.quit(activityId);
    }

    /**
     * 管理名单。仅发起人，含已退出/已移除。
     */
    @GetMapping("/{activityId}/members")
    public List<MealActivityMemberVO> listMembers(@PathVariable("activityId") Long activityId) {
        return mealActivityService.listMembers(activityId);
    }

    /**
     * 发起人移出一名成员。不能移自己。
     */
    @DeleteMapping("/{activityId}/members/{userId}")
    public MealRemoveResultVO removeMember(
            @PathVariable("activityId") Long activityId,
            @PathVariable("userId") Long userId) {
        return mealActivityService.removeMember(activityId, userId);
    }

    /**
     * 发起人取消活动。原因可选，本期不落库。
     */
    @PostMapping("/{activityId}/cancel")
    public MealCancelResultVO cancel(
            @PathVariable("activityId") Long activityId,
            @RequestBody(required = false) CancelMealActivityDTO dto) {
        return mealActivityService.cancel(activityId, dto);
    }

    /**
     * 活动结束后提交反馈。
     */
    @PostMapping("/{activityId}/feedback")
    public MealFeedbackCreateVO submitFeedback(
            @PathVariable("activityId") Long activityId,
            @RequestBody SubmitMealFeedbackDTO dto) {
        return mealActivityService.submitFeedback(activityId, dto);
    }

    /**
     * 该活动的反馈列表。仅参与成员。
     */
    @GetMapping("/{activityId}/feedback")
    public List<MealFeedbackVO> listFeedback(@PathVariable("activityId") Long activityId) {
        return mealActivityService.listFeedback(activityId);
    }
}
