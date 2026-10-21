package com.campus.food.campusfood.controller;

import com.campus.food.campusfood.dto.UpdateProfileDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.UserService;
import com.campus.food.campusfood.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
