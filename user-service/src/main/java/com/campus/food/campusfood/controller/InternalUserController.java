package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.UserStatusUpdateDTO;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.service.UserService;
import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.UserListVO;
import com.campus.food.campusfood.vo.UserStatusUpdateVO;

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

    /**
     * 管理端用户分页，由 merchant-service 经 Feign 调用。
     *
     * @param keyword 昵称 / 手机 / 邮箱，可省略
     * @param status  0 封禁 / 1 正常，可省略
     * @param page    页码
     * @param size    每页条数
     * @return 分页列表
     */
    @GetMapping("/users")
    public PageVO<UserListVO> listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "status", required = false) Integer status,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "size", required = false) Integer size) {
        return userService.listUsersForAdmin(keyword, status, page, size);
    }

    /**
     * 封禁 / 恢复用户，由 merchant-service 经 Feign 调用。
     *
     * <p>业务失败以 {@code success=false} 返回，避免 Feign 把 400 转成 500。
     *
     * @param id         用户主键
     * @param operatorId 操作管理员 ID
     * @param dto        目标状态与原因
     * @return 执行结果；失败时带原因
     */
    @PutMapping("/users/{id}/status")
    public UserStatusUpdateVO setUserStatus(@PathVariable("id") Long id,
                                            @RequestParam("operatorId") Long operatorId,
                                            @RequestBody UserStatusUpdateDTO dto) {
        try {
            userService.setUserStatus(id, dto, operatorId);
            return UserStatusUpdateVO.ok();
        } catch (BizException e) {
            return UserStatusUpdateVO.fail(e.getMessage());
        }
    }
}
