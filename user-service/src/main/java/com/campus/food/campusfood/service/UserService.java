package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.UpdateProfileDTO;
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
}
