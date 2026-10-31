package com.campus.food.campusfood.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.service.SocialService;
import com.campus.food.campusfood.dto.CreateSessionDTO;
import com.campus.food.campusfood.dto.SendMessageDTO;
import com.campus.food.campusfood.vo.ChatMessagePageVO;
import com.campus.food.campusfood.vo.ChatMessageVO;
import com.campus.food.campusfood.vo.ChatSessionVO;
import com.campus.food.campusfood.vo.FriendRequestVo;
import com.campus.food.campusfood.vo.FriendVo;

import jakarta.validation.Valid;

/**
 * 社交接口：好友申请、好友列表与删除。均需 JWT，身份取自当前登录用户。
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

    /**
     * 当前登录用户的好友列表。
     *
     * @return 好友项列表，只含昵称与头像
     * @throws BizException 未登录时
     */
    @GetMapping("/friends")
    public List<FriendVo> getFriends() {
        return socialService.getFriends();
    }

    /**
     * 当前登录用户收到的好友申请（收件箱），仅待处理。
     *
     * @return 待处理申请列表
     * @throws BizException 未登录时
     */
    @GetMapping("/friend-requests/inbox")
    public List<FriendRequestVo> getFriendRequests() {
        return socialService.getFriendRequests();
    }

    /**
     * 删除好友，双向解除关系并清理 Redis 关注集合。
     *
     * @param friendId 好友用户 ID
     * @throws BizException 未登录、好友 ID 为空或关系不存在时
     */
    @PostMapping("/friends/{friendId}/delete")
    public void deleteFriend(@PathVariable("friendId") Long friendId) {
        socialService.deleteFriend(friendId);
    }

    /**
     * 创建或返回已有私聊会话。
     */
    @PostMapping("/sessions")
    public ChatSessionVO createOrGetSession(@RequestBody CreateSessionDTO dto) {
        return socialService.createOrGetSession(dto);
    }

    /**
     * 拉取会话历史，按时间倒序。
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ChatMessagePageVO listMessages(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam(required = false) String beforeMsgId,
            @RequestParam(required = false) Integer size) {
        return socialService.listMessages(sessionId, beforeMsgId, size);
    }

    /**
     * 向会话发送一条消息。
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ChatMessageVO sendMessage(
            @PathVariable("sessionId") Long sessionId,
            @RequestBody SendMessageDTO dto) {
        return socialService.sendMessage(sessionId, dto);
    }
}
