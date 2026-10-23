package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.service.UserService;

/**
 * 用户服务对内 HTTP 接口，仅供其他微服务通过 Feign 调用。
 *
 * <p>路径 {@code /internal/**} 在 {@link com.campus.food.campusfood.config.WebMvcConfig} 中排除 JWT 拦截，
 * 不对外暴露在 API Gateway 路由中。
 *
 * @author yang
 * @since 2026-10-22
 */
@RestController
@RequestMapping("/internal")
public class InternalUserController {

    @Autowired
    private UserService userService;

    /**
     * 按 ID 返回推荐等场景需要的用户摘要。
     *
     * @param id 用户主键
     * @return 用户摘要
     * @throws com.campus.food.campusfood.exception.BizException 用户不存在或已删除时
     */
    @GetMapping("/users/{id}")
    public UserInfo getUserInfo(@PathVariable("id") Long id) {
        return userService.getUserInfo(id);
    }

    /**
     * 将用户角色更新为商家。
     *
     * @param id 用户主键
     * @throws com.campus.food.campusfood.exception.BizException 更新失败时
     */
    @PutMapping("/users/{id}/merchant-role")
    public void setMerchantRole(@PathVariable("id") Long id) {
        userService.setMerchantRole(id);
    }
}
