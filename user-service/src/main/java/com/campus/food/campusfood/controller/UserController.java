package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.dto.AddFavoritiesDTO;
import com.campus.food.campusfood.dto.AddFeedDTO;
import com.campus.food.campusfood.dto.DeleteFavoritiesDTO;
import com.campus.food.campusfood.dto.ReminderDTO;
import com.campus.food.campusfood.dto.UpdateProfileDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.UserService;
import com.campus.food.campusfood.vo.FavoriteAddVO;
import com.campus.food.campusfood.vo.FavoritiesVO;
import com.campus.food.campusfood.vo.FeedPublishVO;
import com.campus.food.campusfood.vo.FeedVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.RemindVO;
import com.campus.food.campusfood.vo.UserProfileVO;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户资料接口，均需 JWT。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查看当前登录用户资料，需 JWT。
     *
     * @return 用户资料，不含密码
     * @throws BizException 未登录或用户已删除时
     */
    @GetMapping("/profile")
    public UserProfileVO profile() {
        return userService.getCurrentProfile();
    }

    /**
     * 更新当前登录用户资料，需 JWT。
     *
     * @param dto 待更新字段，未传的字段不修改
     * @return 更新后的用户资料，不含密码
     * @throws BizException 未登录、用户已删除或字段不合法时
     */
    @PutMapping("/profile")
    public UserProfileVO updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return userService.updateCurrentProfile(dto);
    }

    /**
     * 当前用户收藏列表。
     */
    @GetMapping("/favorites")
    public PageVO<FavoritiesVO> favoritiesList(
            @RequestParam(required = false) Integer targetType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return userService.getFavoritiesList(targetType, page, size);
    }

    /**
     * 添加收藏，成功体含 {@code favorited=true}。
     */
    @PostMapping("/favorites")
    public FavoriteAddVO addFavorities(@Valid @RequestBody AddFavoritiesDTO dto) {
        return userService.addFavorities(dto);
    }

    /**
     * 取消收藏，成功体含 {@code favorited=false}。
     */
    @DeleteMapping("/favorites")
    public FavoriteAddVO deleteFavorities(@Valid @RequestBody DeleteFavoritiesDTO dto) {
        return userService.deleteFavorities(dto);
    }

    /**
     * 当前用户三类提醒开关（优惠活动 / 好友动态 / 订单状态），需 JWT。任意角色可调。
     *
     * @return 固定三条开关；缺行时按默认开启补齐
     * @throws BizException 未登录时
     */
    @GetMapping("/reminders")
    public List<RemindVO> listReminders() {
        return userService.listReminders();
    }

    /**
     * 拨其中一个提醒开关，需 JWT。关订单提醒后订单页仍可看状态，只是不推站内信。
     *
     * @param dto remindType、enabled，channel 不传视为 INBOX
     * @return 更新后的该项开关
     * @throws BizException 未登录、类型不支持或 enabled 为空时
     */
    @PutMapping("/reminders")
    public RemindVO updateReminder(@Valid @RequestBody ReminderDTO dto) {
        return userService.updateReminder(dto);
    }

    @GetMapping("/feeds")
    public PageVO<FeedVO> listFeeds(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return userService.listFeeds(page, size);
    }

    @PostMapping("/feeds")
    public FeedPublishVO addFeed(@Valid @RequestBody AddFeedDTO dto) {
        return userService.addFeed(dto);
    }

    @DeleteMapping("/feeds/{id}")
    public void deleteFeed(@PathVariable Long id) {
        userService.deleteFeed(id);
    }
}
