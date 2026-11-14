package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.AddFavoritiesDTO;
import com.campus.food.campusfood.dto.AddFeedDTO;
import com.campus.food.campusfood.dto.DeleteFavoritiesDTO;
import com.campus.food.campusfood.dto.ReminderDTO;
import com.campus.food.campusfood.dto.UpdateProfileDTO;
import com.campus.food.campusfood.dto.UserStatusUpdateDTO;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.vo.FavoriteAddVO;
import com.campus.food.campusfood.vo.FavoritiesVO;
import com.campus.food.campusfood.vo.FeedPublishVO;
import com.campus.food.campusfood.vo.FeedVO;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.RemindVO;
import com.campus.food.campusfood.vo.UserListVO;
import com.campus.food.campusfood.vo.UserProfileVO;

/**
 * 当前登录用户资料服务。
 *
 * @author yang
 * @since 2026-10-16
 */
public interface UserService {

    /**
     * 根据当前登录用户 ID 返回资料（不含密码）。
     *
     * @return 用户资料
     * @throws com.campus.food.campusfood.exception.BizException 未登录或用户已删除时
     */
    UserProfileVO getCurrentProfile();

    /**
     * 更新当前登录用户资料，返回更新后的资料（不含密码）。
     *
     * @param dto 待更新字段，未传的字段不修改
     * @return 更新后的用户资料
     * @throws com.campus.food.campusfood.exception.BizException 未登录、用户已删除或字段不合法时
     */
    UserProfileVO updateCurrentProfile(UpdateProfileDTO dto);

    /**
     * 按 ID 查询内部调用用的用户摘要（Feign / 推荐等）。
     *
     * @param id 用户主键，不可为 null
     * @return 不含密码的 {@link UserInfo}
     * @throws com.campus.food.campusfood.exception.BizException 用户不存在或已删除时
     */
    UserInfo getUserInfo(Long id);

    /**
     * 将用户角色设为商家（入驻审核通过后调用）。
     *
     * @param id 用户主键，不可为 null
     * @throws com.campus.food.campusfood.exception.BizException 用户不存在或更新失败时
     */
    void setMerchantRole(Long id);

    /**
     * 管理端用户分页列表（未删除账号）。
     *
     * @param keyword 昵称 / 手机 / 邮箱模糊匹配，空表示不限
     * @param status  0 封禁 / 1 正常，null 表示不限
     * @param page    页码，从 1 起
     * @param size    每页条数
     * @return 分页结果，无数据时 {@code list} 为空
     * @throws com.campus.food.campusfood.exception.BizException status 不是 0/1 时
     */
    PageVO<UserListVO> listUsersForAdmin(String keyword, Integer status, Integer page, Integer size);

    /**
     * 封禁或恢复用户。只允许管理员操作普通用户，封禁时写入处罚记录。
     *
     * @param id         用户主键
     * @param dto        目标状态与原因
     * @param operatorId 操作管理员 ID
     * @throws com.campus.food.campusfood.exception.BizException 用户不存在、目标是管理员、
     *                                                           状态不合法或封禁原因为空时
     */
    void setUserStatus(Long id, UserStatusUpdateDTO dto, Long operatorId);

    /**
     * 当前用户收藏列表。
     *
     * @param targetType 1 美食 / 2 店铺，null 表示全部
     * @param page       页码，从 1 起
     * @param size       每页条数
     * @return 分页卡片
     * @throws com.campus.food.campusfood.exception.BizException 未登录或类型不合法时
     */
    PageVO<FavoritiesVO> getFavoritiesList(Integer targetType, Integer page, Integer size);

    /**
     * 收藏上架美食或营业中店铺。
     *
     * @param dto targetType + targetId
     * @return favorited 为 true
     * @throws com.campus.food.campusfood.exception.BizException 未登录、目标不可收藏或已收藏时
     */
    FavoriteAddVO addFavorities(AddFavoritiesDTO dto);

    /**
     * 取消收藏。
     *
     * @param dto targetType + targetId
     * @return favorited 为 false
     * @throws com.campus.food.campusfood.exception.BizException 未登录或未收藏过时
     */
    FavoriteAddVO deleteFavorities(DeleteFavoritiesDTO dto);

    /**
     * 当前用户三类提醒开关；缺行按默认开启、渠道 INBOX 补齐。
     *
     * @return 固定顺序：CAMPAIGN、FRIEND_FEED、ORDER
     * @throws com.campus.food.campusfood.exception.BizException 未登录时
     */
    List<RemindVO> listReminders();

    /**
     * 更新当前用户的一项提醒开关（要不要收该类站内信）。
     *
     * @param dto remindType、enabled，channel 可选（仅 INBOX）
     * @return 更新后的开关
     * @throws com.campus.food.campusfood.exception.BizException 未登录、类型不支持或 enabled
     *                                                           为空时
     */
    RemindVO updateReminder(ReminderDTO dto);

    PageVO<FeedVO> listFeeds(Integer page, Integer size);

    FeedPublishVO addFeed(AddFeedDTO dto);

    void deleteFeed(Long id);
}
