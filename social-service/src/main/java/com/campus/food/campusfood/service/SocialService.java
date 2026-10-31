package com.campus.food.campusfood.service;

import java.util.List;

import com.campus.food.campusfood.dto.CreateSessionDTO;
import com.campus.food.campusfood.dto.SendMessageDTO;
import com.campus.food.campusfood.vo.ChatMessagePageVO;
import com.campus.food.campusfood.vo.ChatMessageVO;
import com.campus.food.campusfood.vo.ChatSessionVO;
import com.campus.food.campusfood.vo.FriendRequestVo;
import com.campus.food.campusfood.vo.FriendVo;

/**
 * 社交业务门面：编排好友申请、同意/拒绝与好友关系维护。
 *
 * <p>底层 {@code *ServiceImpl} 只做单表存取，申请判重、隐私校验、
 * 双向关系与 Redis 关注集合的一致性都在这一层处理。
 *
 * @author yang
 * @since 2026-10-23
 */
public interface SocialService {

    /**
     * 发送好友申请。
     *
     * <p>申请人取当前登录用户，忽略入参中的 {@code fromUserId}；
     * 状态固定为待处理。会校验目标用户存在、对方允许被添加、
     * 双方尚不是好友且没有待处理申请。
     *
     * @param friendRequestVo 至少包含 {@code toUserId}，可附带 {@code message}
     * @return 落库后的申请
     * @throws com.campus.food.campusfood.exception.BizException 未登录、目标用户不合法或重复申请时
     */
    FriendRequestVo sendFriendRequest(FriendRequestVo friendRequestVo);

    /**
     * 处理好友申请：同意或拒绝。
     *
     * <p>只有被申请人可以处理，且申请必须仍处于待处理状态。
     * 同意时写入双向好友关系并更新 Redis 关注/粉丝集合；
     * 拒绝只把状态改为已拒绝。
     *
     * @param friendRequestVo 含 {@code id} 与目标 {@code status}（1 同意，2 拒绝）
     * @return 更新后的申请
     * @throws com.campus.food.campusfood.exception.BizException 申请不存在、无权处理或状态已变更时
     */
    FriendRequestVo handleFriendRequest(FriendRequestVo friendRequestVo);

    /**
     * 查询当前用户的好友列表。
     *
     * <p>逐个好友向 user-service 取昵称与头像；单个好友查不到时跳过，
     * 不影响其余好友返回。
     *
     * @return 好友列表；没有好友时返回空列表
     * @throws com.campus.food.campusfood.exception.BizException 未登录时
     */
    List<FriendVo> getFriends();

    /**
     * 删除好友。
     *
     * <p>复用 {@code FriendshipService#deletePair}，双向删除关系行并清理 Redis 关注/粉丝集合。
     *
     * @param friendId 好友用户 ID
     * @throws com.campus.food.campusfood.exception.BizException 未登录、好友 ID 为空或关系不存在时
     */
    void deleteFriend(Long friendId);

    /**
     * 查询当前用户收到的好友申请（收件箱）。
     *
     * @return 待处理申请列表；没有时返回空列表
     * @throws com.campus.food.campusfood.exception.BizException 未登录时
     */
    List<FriendRequestVo> getFriendRequests();

    /**
     * 创建或返回已有私聊会话。
     *
     * @param dto peerUserId、sessionType（本期仅 1）
     * @return 会话摘要
     */
    ChatSessionVO createOrGetSession(CreateSessionDTO dto);

    /**
     * 拉取会话历史，时间倒序。
     *
     * @param sessionId   会话 ID
     * @param beforeMsgId 更早于该 Mongo 消息 ID，可为 null
     * @param size        条数
     * @return 分页
     */
    ChatMessagePageVO listMessages(Long sessionId, String beforeMsgId, Integer size);

    /**
     * 发送一条消息。
     *
     * @param sessionId 会话 ID
     * @param dto       类型与正文
     * @return 落库后的消息
     */
    ChatMessageVO sendMessage(Long sessionId, SendMessageDTO dto);
}
