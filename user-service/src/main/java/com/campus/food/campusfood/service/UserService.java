package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.UpdateProfileDTO;
import com.campus.food.campusfood.feign.UserInfo;
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
}
