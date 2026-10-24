package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.SocialService;
import com.campus.food.campusfood.vo.FriendRequestVo;

import jakarta.validation.Valid;

/**
 * 社交接口：好友申请的发送与处理。均需 JWT，身份取自当前登录用户。
 *
 * @author yang
 * @since 2026-10-23
 */
@RestController
@RequestMapping("/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    /**
     * 发送好友申请。请求体只需 {@code toUserId}，可附带 {@code message}；
     * 申请人由服务端按当前登录用户写入。
     *
     * @param friendRequestVo 申请信息
     * @return 落库后的申请
     * @throws BizException 未登录、目标用户不合法或重复申请时
     */
    @PostMapping("/sendFriendRequest")
    public FriendRequestVo sendFriendRequest(@Valid @RequestBody FriendRequestVo friendRequestVo) {
        return socialService.sendFriendRequest(friendRequestVo);
    }

    /**
     * 处理好友申请。请求体需 {@code id} 和 {@code status}（1 同意，2 拒绝）。
     *
     * @param friendRequestVo 处理信息
     * @return 更新后的申请
     * @throws BizException 申请不存在、无权处理或状态已变更时
     */
    @PostMapping("/handleFriendRequest")
    public FriendRequestVo handleFriendRequest(@Valid @RequestBody FriendRequestVo friendRequestVo) {
        return socialService.handleFriendRequest(friendRequestVo);
    }
}
