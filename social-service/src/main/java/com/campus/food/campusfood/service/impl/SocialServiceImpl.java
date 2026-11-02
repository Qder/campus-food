package com.campus.food.campusfood.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.common.SocialRedisKeys;
import com.campus.food.campusfood.dto.BlockUserDTO;
import com.campus.food.campusfood.dto.CreateSessionDTO;
import com.campus.food.campusfood.dto.SendMessageDTO;
import com.campus.food.campusfood.entity.ChatMessage;
import com.campus.food.campusfood.entity.ChatMessageIndex;
import com.campus.food.campusfood.entity.ChatSession;
import com.campus.food.campusfood.entity.ChatSessionMember;
import com.campus.food.campusfood.entity.UserBlacklist;
import com.campus.food.campusfood.entity.FriendRequest;
import com.campus.food.campusfood.entity.Friendship;
import com.campus.food.campusfood.exception.BizException;
import com.campus.food.campusfood.feign.UserFeignClient;
import com.campus.food.campusfood.feign.UserInfo;
import com.campus.food.campusfood.mapper.ChatMessageIndexMapper;
import com.campus.food.campusfood.mapper.ChatSessionMapper;
import com.campus.food.campusfood.mapper.ChatSessionMemberMapper;
import com.campus.food.campusfood.mapper.FriendRequestMapper;
import com.campus.food.campusfood.mapper.FriendshipMapper;
import com.campus.food.campusfood.mapper.UserBlacklistMapper;
import com.campus.food.campusfood.repository.ChatMessageRepository;
import com.campus.food.campusfood.service.FriendshipService;
import com.campus.food.campusfood.service.SocialService;
import com.campus.food.campusfood.vo.BlacklistItemVO;
import com.campus.food.campusfood.vo.BlockResultVO;
import com.campus.food.campusfood.vo.ChatMessagePageVO;
import com.campus.food.campusfood.vo.ChatMessageVO;
import com.campus.food.campusfood.vo.ChatSessionVO;
import com.campus.food.campusfood.vo.FriendRequestVo;
import com.campus.food.campusfood.vo.FriendVo;

import feign.FeignException;

/**
 * 社交业务门面实现：好友申请与处理、私聊会话与消息。
 *
 * <p>
 * 申请阶段的判重与隐私校验、同意阶段的双向关系与 Redis 关注集合
 * 都在这一层完成。私聊同样走这一层：MySQL 管会话/成员/索引，Mongo 管正文，
 * 底层 Mapper / Repository 只负责单表读写。
 *
 * @author yang
 * @since 2026-10-23
 */
@Service
public class SocialServiceImpl implements SocialService {

    private static final Logger log = LoggerFactory.getLogger(SocialServiceImpl.class);

    /** 申请状态：待处理 */
    private static final Integer STATUS_PENDING = 0;
    /** 申请状态：已同意 */
    private static final Integer STATUS_ACCEPTED = 1;
    /** 申请状态：已拒绝 */
    private static final Integer STATUS_REJECTED = 2;

    /** 留言最大长度，与 {@code friend_request.message VARCHAR(255)} 对齐 */
    private static final int MESSAGE_MAX_LENGTH = 255;

    /** 会话类型：1 私聊（本期只允许这个值） */
    private static final int SESSION_TYPE_PRIVATE = 1;
    /** TEXT 正文最大字数，与契约 4.6.8 对齐 */
    private static final int TEXT_MAX_LENGTH = 1000;
    /** 会话列表预览截断长度，对齐 {@code chat_session.last_msg_preview VARCHAR(255)} */
    private static final int PREVIEW_MAX_LENGTH = 255;
    /** 允许的消息类型 */
    private static final Set<String> MSG_TYPES = Set.of("TEXT", "IMAGE", "EMOJI");

    private final FriendRequestMapper friendRequestMapper;
    private final FriendshipMapper friendshipMapper;
    private final UserFeignClient userFeignClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final FriendshipService friendshipService;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionMemberMapper chatSessionMemberMapper;
    private final ChatMessageIndexMapper chatMessageIndexMapper;
    private final UserBlacklistMapper userBlacklistMapper;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 构造注入 Mapper、Feign 客户端与 Redis 模板。
     *
     * @param friendRequestMapper     好友申请 Mapper
     * @param friendshipMapper        好友关系 Mapper
     * @param userFeignClient         用户服务内部接口
     * @param stringRedisTemplate     Redis 字符串模板
     * @param friendshipService       好友关系服务，删除好友时复用其双向删除与 Redis 清理
     * @param chatSessionMapper       会话表
     * @param chatSessionMemberMapper 会话成员表
     * @param chatMessageIndexMapper  消息索引表
     * @param userBlacklistMapper     拉黑只读查询
     * @param chatMessageRepository   Mongo 消息正文
     */
    public SocialServiceImpl(FriendRequestMapper friendRequestMapper,
            FriendshipMapper friendshipMapper,
            UserFeignClient userFeignClient,
            StringRedisTemplate stringRedisTemplate,
            FriendshipService friendshipService,
            ChatSessionMapper chatSessionMapper,
            ChatSessionMemberMapper chatSessionMemberMapper,
            ChatMessageIndexMapper chatMessageIndexMapper,
            UserBlacklistMapper userBlacklistMapper,
            ChatMessageRepository chatMessageRepository) {
        this.friendRequestMapper = friendRequestMapper;
        this.friendshipMapper = friendshipMapper;
        this.userFeignClient = userFeignClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.friendshipService = friendshipService;
        this.chatSessionMapper = chatSessionMapper;
        this.chatSessionMemberMapper = chatSessionMemberMapper;
        this.chatMessageIndexMapper = chatMessageIndexMapper;
        this.userBlacklistMapper = userBlacklistMapper;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 申请人一律取 {@link UserContext}，入参中的 {@code fromUserId} 被忽略；
     * 申请状态由服务端置为待处理，留言去空白并截断到列宽。
     */
    @Override
    public FriendRequestVo sendFriendRequest(FriendRequestVo friendRequestVo) {
        Long curId = requireCurUserId();
        if (friendRequestVo == null || friendRequestVo.getToUserId() == null) {
            throw new BizException("好友ID不能为空");
        }
        Long toUserId = friendRequestVo.getToUserId();
        if (curId.equals(toUserId)) {
            throw new BizException("不能添加自己为好友");
        }

        UserInfo target = loadUser(toUserId);
        if (target.getDeleted() != null && target.getDeleted() != 0) {
            throw new BizException("用户不存在");
        }
        // 拉黑后不能互相发申请；移出黑名单后才可以
        if (userBlacklistMapper.countPair(toUserId, curId) > 0) {
            throw new BizException("你已被对方拉黑");
        }
        if (userBlacklistMapper.countPair(curId, toUserId) > 0) {
            throw new BizException("你已将对方拉黑，请先移出黑名单");
        }
        if (!Integer.valueOf(1).equals(target.getPrivacyAddFriend())) {
            throw new BizException("对方不允许添加好友");
        }

        if (friendshipMapper.selectByUserIdAndFriendId(curId, toUserId) > 0) {
            throw new BizException("已经是好友");
        }
        // 任一方向存在待处理申请都不再新建，避免同一条关系反复申请
        if (friendRequestMapper.selectPending(curId, toUserId) > 0) {
            throw new BizException("已申请过，请等待对方处理");
        }
        if (friendRequestMapper.selectPending(toUserId, curId) > 0) {
            throw new BizException("对方已向你发送申请，请先在申请列表中处理");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(curId);
        request.setToUserId(toUserId);
        request.setMessage(normalizeMessage(friendRequestVo.getMessage()));
        request.setStatus(STATUS_PENDING);
        friendRequestMapper.insert(request);

        return toVo(friendRequestMapper.selectById(request.getId()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 只有被申请人本人可以处理，且申请必须仍处于待处理状态；
     * 同意时先建双向关系再回填状态，两者同事务，任一步失败整体回滚。
     * Redis 关注集合在库写成功后才更新，失败只记日志并以库为准。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FriendRequestVo handleFriendRequest(FriendRequestVo friendRequestVo) {
        Long curId = requireCurUserId();
        if (friendRequestVo == null || friendRequestVo.getId() == null) {
            throw new BizException("申请ID不能为空");
        }
        Integer targetStatus = friendRequestVo.getStatus();
        if (targetStatus == null) {
            throw new BizException("处理结果不能为空，请传 1（同意）或 2（拒绝）");
        }
        if (!STATUS_ACCEPTED.equals(targetStatus) && !STATUS_REJECTED.equals(targetStatus)) {
            throw new BizException("status 只能传 1（同意）或 2（拒绝），当前传的是 " + targetStatus);
        }

        FriendRequest request = friendRequestMapper.selectById(friendRequestVo.getId());
        if (request == null) {
            throw new BizException("申请不存在");
        }
        if (!curId.equals(request.getToUserId())) {
            // 自己发出的申请，说明是拿错了 id 或拿错了账号
            if (curId.equals(request.getFromUserId())) {
                throw new BizException("这是你发出的申请，需等待对方处理");
            }
            log.warn("处理好友申请被拒: requestId={}, 当前用户={}, 申请接收人={}",
                    request.getId(), curId, request.getToUserId());
            throw new BizException("该申请不是发给你的，无权处理");
        }
        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new BizException("申请已处理");
        }

        if (STATUS_ACCEPTED.equals(targetStatus)) {
            // 处理期间若已被拉黑，不能再结成好友
            if (userBlacklistMapper.countPair(curId, request.getFromUserId()) > 0
                    || userBlacklistMapper.countPair(request.getFromUserId(), curId) > 0) {
                throw new BizException("双方存在拉黑关系，无法同意申请");
            }
            addFriendPair(request.getFromUserId(), request.getToUserId());
        }

        request.setStatus(targetStatus);
        request.setHandledAt(LocalDateTime.now());
        // update 会按主键覆盖状态，此时只有一条待处理记录会走到这里
        friendRequestMapper.update(request);

        return toVo(friendRequestMapper.selectById(request.getId()));
    }

    /**
     * 建立双向好友关系，并同步 Redis 关注/粉丝集合。
     *
     * @param fromUserId 申请人
     * @param toUserId   同意人
     */
    private void addFriendPair(Long fromUserId, Long toUserId) {
        // 已有关系时只补推导结果，避免唯一键 uk_friendship 冲突
        if (friendshipMapper.selectByUserIdAndFriendId(fromUserId, toUserId) == 0) {
            friendshipMapper.insert(buildFriendship(fromUserId, toUserId));
        }
        if (friendshipMapper.selectByUserIdAndFriendId(toUserId, fromUserId) == 0) {
            friendshipMapper.insert(buildFriendship(toUserId, fromUserId));
        }
        syncFollowCache(fromUserId, toUserId);
    }

    /**
     * 把双向关注关系写入 Redis Set：A 关注 B、B 关注 A，同时互为粉丝。
     *
     * @param fromUserId 申请人
     * @param toUserId   同意人
     */
    private void syncFollowCache(Long fromUserId, Long toUserId) {
        String from = String.valueOf(fromUserId);
        String to = String.valueOf(toUserId);
        try {
            // 将fromUserId关注toUserId，将toUserId关注fromUserId，将fromUserId的粉丝列表中添加toUserId，将toUserId的粉丝列表中添加fromUserId
            stringRedisTemplate.opsForSet().add(SocialRedisKeys.follow(fromUserId), to);
            stringRedisTemplate.opsForSet().add(SocialRedisKeys.fans(toUserId), from);
            stringRedisTemplate.opsForSet().add(SocialRedisKeys.follow(toUserId), from);
            stringRedisTemplate.opsForSet().add(SocialRedisKeys.fans(fromUserId), to);
        } catch (Exception e) {
            // Redis 不可用不影响好友关系成立，读取时回源数据库重建
            log.warn("同意好友申请后同步关注集合失败, from={}, to={}", fromUserId, toUserId, e);
        }
    }

    /**
     * 构造单向好友关系。
     *
     * @param userId   用户 ID
     * @param friendId 好友 ID
     * @return 待插入的关系
     */
    private Friendship buildFriendship(Long userId, Long friendId) {
        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        return friendship;
    }

    /**
     * 取当前登录用户 ID。
     *
     * @return 当前用户 ID
     * @throws BizException 未登录时
     */
    private Long requireCurUserId() {
        Long curId = UserContext.getUserId();
        if (curId == null) {
            throw new BizException("请先登录");
        }
        return curId;
    }

    /**
     * 去掉留言首尾空白，空串归一为 null，超长按 {@code message VARCHAR(255)} 截断。
     *
     * @param message 原始留言，可为 null
     * @return 可直接落库的留言
     */
    private String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }
        String text = message.trim();
        if (text.isEmpty()) {
            return null;
        }
        return text.length() > MESSAGE_MAX_LENGTH ? text.substring(0, MESSAGE_MAX_LENGTH) : text;
    }

    /**
     * 通过 user-service 查询用户摘要。
     *
     * @param userId 用户 ID
     * @return 用户摘要
     * @throws BizException 用户不存在或用户服务不可用时
     */
    private UserInfo loadUser(Long userId) {
        UserInfo user;
        try {
            user = userFeignClient.getUserInfo(userId);
        } catch (FeignException e) {
            // user-service 对不存在用户返回 400，统一转成业务提示
            throw new BizException("用户不存在");
        }
        // 熔断降级返回 null，与「用户不存在」对上层是同一结果
        if (user == null || user.getId() == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /**
     * 实体转对外视图。
     *
     * @param request 申请实体
     * @return 对外视图；入参为 null 时返回 null
     */
    private FriendRequestVo toVo(FriendRequest request) {
        if (request == null) {
            return null;
        }
        FriendRequestVo vo = new FriendRequestVo();
        vo.setId(request.getId());
        vo.setFromUserId(request.getFromUserId());
        vo.setToUserId(request.getToUserId());
        vo.setMessage(request.getMessage());
        vo.setStatus(request.getStatus());
        vo.setCreatedAt(request.getCreatedAt());
        vo.setHandledAt(request.getHandledAt());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 逐个好友调 user-service 查昵称与头像，单个好友查不到时跳过，
     * 不让一条坏数据把整个列表打成 500。
     */
    @Override
    public List<FriendVo> getFriends() {
        Long curId = requireCurUserId();
        List<Friendship> friendships = friendshipMapper.selectByUserId(curId);
        List<FriendVo> friends = new ArrayList<>();
        for (Friendship friendship : friendships) {
            try {
                friends.add(toFriendVo(loadUser(friendship.getFriendId())));
            } catch (BizException e) {
                // 好友账号已注销或用户服务瞬时不可用，跳过该条
                log.warn("好友信息查询失败，已跳过, friendId={}", friendship.getFriendId());
            }
        }
        return friends;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFriend(Long friendId) {
        Long curId = requireCurUserId();
        if (friendId == null) {
            throw new BizException("好友ID不能为空");
        }
        friendshipService.deletePair(curId, friendId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 只返回待处理（{@code status=0}）的申请，已同意/已拒绝的不再出现在收件箱。
     */
    @Override
    public List<FriendRequestVo> getFriendRequests() {
        Long curId = requireCurUserId();
        List<FriendRequest> requests = friendRequestMapper.selectInbox(curId, STATUS_PENDING);
        List<FriendRequestVo> result = new ArrayList<>();
        for (FriendRequest request : requests) {
            FriendRequestVo vo = toVo(request);
            // 带上申请人昵称，收件箱就不必再按 id 反查
            try {
                vo.setFromNickname(loadUser(request.getFromUserId()).getNickname());
            } catch (BizException e) {
                log.warn("申请人信息查询失败, fromUserId={}", request.getFromUserId());
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 用户摘要转好友列表项。
     *
     * @param user 用户摘要
     * @return 只含展示字段的好友项
     */
    private FriendVo toFriendVo(UserInfo user) {
        FriendVo vo = new FriendVo();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 同一对好友只保留一场 {@code sessionType=1} 的私聊：两边都在
     * {@code chat_session_member} 里即视为已有，不看 {@code peer_user_id} 方向。
     * 没有则插入一行会话 + 两行成员（自己、对方），未读先写 0。
     */
    @Override
    @Transactional
    public ChatSessionVO createOrGetSession(CreateSessionDTO dto) {
        Long curId = requireCurUserId();
        if (dto == null || dto.getPeerUserId() == null) {
            throw new BizException("对方用户不能为空");
        }
        // 群聊本期不做
        if (dto.getSessionType() == null || dto.getSessionType() != SESSION_TYPE_PRIVATE) {
            throw new BizException("本期仅支持私聊");
        }
        Long peerId = dto.getPeerUserId();
        if (curId.equals(peerId)) {
            throw new BizException("不能与自己发起会话");
        }
        // 查看对方账号是否存在（走 user-service）
        loadUser(peerId);
        // 查看是否已是好友（friendship 单向查当前用户视角即可，关系是双向各一行）
        if (friendshipMapper.selectByUserIdAndFriendId(curId, peerId) <= 0) {
            throw new BizException("请先添加对方为好友");
        }
        // 查看是否被对方拉黑、或自己已拉黑对方
        assertNotBlocked(curId, peerId, true);
        // 查看两人是否已有私聊会话
        ChatSession existing = chatSessionMapper.selectPrivateBetween(curId, peerId);
        if (existing == null) {
            // 新建会话：peer_user_id 记创建人眼里的对方，查找仍以成员表为准
            ChatSession session = new ChatSession();
            session.setSessionType(SESSION_TYPE_PRIVATE);
            session.setPeerUserId(peerId);
            chatSessionMapper.insert(session);
            insertMember(session.getId(), curId);
            insertMember(session.getId(), peerId);
            existing = session;
        }
        // 昵称头像现查 Feign，不写进会话表
        return toSessionVo(existing.getId(), peerId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * {@code beforeMsgId} 为空取最新一页；有值则先按 Mongo {@code _id} 取出该条的
     * {@code createdAt}，再查更早的。{@code hasMore} 等于本页条数是否凑满 {@code size}。
     * 列表时间倒序，{@code msgId} 用 Mongo 主键字符串。
     */
    @Override
    public ChatMessagePageVO listMessages(Long sessionId, String beforeMsgId, Integer size) {
        Long curId = requireCurUserId();
        // 必须是该会话成员
        requireSessionMember(sessionId, curId);
        // size 默认 20，最大 50
        int s = size == null || size < 1 ? 20 : Math.min(size, 50);
        List<ChatMessage> rows;
        if (beforeMsgId == null || beforeMsgId.isBlank()) {
            // 进入聊天页：Mongo 本会话按发送时间 createdAt 倒序，取最新 size 条
            rows = chatMessageRepository.findBySessionIdOrderByCreatedAtDesc(
                    sessionId, PageRequest.of(0, s));
        } else {
            // 上滑：游标必须属于本会话，否则当成消息不存在
            ChatMessage cursor = chatMessageRepository.findById(beforeMsgId)
                    .orElseThrow(() -> new BizException("消息不存在"));
            if (!sessionId.equals(cursor.getSessionId())) {
                throw new BizException("消息不存在");
            }
            rows = chatMessageRepository.findBySessionIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                    sessionId, cursor.getCreatedAt(), PageRequest.of(0, s));
        }
        // 同一页里对方昵称只 Feign 一次
        Map<Long, String> nickCache = new HashMap<>();
        List<ChatMessageVO> list = new ArrayList<>();
        for (ChatMessage row : rows) {
            list.add(toMessageVo(row, curId, nickCache));
        }
        ChatMessagePageVO page = new ChatMessagePageVO();
        page.setSessionId(sessionId);
        page.setHasMore(list.size() == s);
        page.setList(list);
        return page;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 每次发送前再查一次拉黑。写入顺序：Mongo 正文 → MySQL 索引 → 会话
     * {@code last_msg_preview}/{@code last_msg_at}。Mongo 不在本地事务里。
     */
    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long sessionId, SendMessageDTO dto) {
        Long curId = requireCurUserId();
        // 必须是该会话成员
        requireSessionMember(sessionId, curId);
        // 私聊另一人；查到才做拉黑拦截（成员异常时不误拦）
        Long peerId = chatSessionMemberMapper.selectPeerUserId(sessionId, curId);
        if (peerId != null) {
            assertNotBlocked(curId, peerId, false);
        }
        if (dto == null || dto.getMsgType() == null || dto.getMsgType().isBlank()) {
            throw new BizException("消息类型不能为空");
        }
        String msgType = dto.getMsgType().trim().toUpperCase();
        if (!MSG_TYPES.contains(msgType)) {
            throw new BizException("不支持的消息类型");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BizException("消息内容不能为空");
        }
        String content = dto.getContent().trim();
        // IMAGE / EMOJI 不按 1000 字限制；IMAGE 的 content 假定已是 URL
        if ("TEXT".equals(msgType) && content.length() > TEXT_MAX_LENGTH) {
            throw new BizException("文字消息不能超过1000字");
        }
        LocalDateTime now = LocalDateTime.now();
        // ① 正文进 Mongo，主键回填到 saved.id，对外就是 msgId
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setFromUserId(curId);
        msg.setMsgType(msgType);
        msg.setContent(content);
        msg.setClientMsgId(dto.getClientMsgId());
        msg.setCreatedAt(now);
        ChatMessage saved = chatMessageRepository.save(msg);

        // ② 索引进 MySQL，mongo_msg_id 与 ① 对齐
        ChatMessageIndex index = new ChatMessageIndex();
        index.setMongoMsgId(saved.getId());
        index.setSessionId(sessionId);
        index.setFromUserId(curId);
        index.setMsgType(msgType);
        index.setCreatedAt(now);
        chatMessageIndexMapper.insert(index);
        // ③ 刷新会话列表预览
        chatSessionMapper.updateLastMessage(sessionId, previewOf(msgType, content), now);

        Map<Long, String> nickCache = new HashMap<>();
        return toMessageVo(saved, curId, nickCache);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 契约要求须已是好友才能拉黑。先写 {@code user_blacklist}，再 {@code deletePair}
     * 双向解除好友，这样好友列表立刻看不到对方；发消息仍走原有拉黑拦截。
     */
    @Override
    @Transactional
    public BlockResultVO blockUser(BlockUserDTO dto) {
        Long curId = requireCurUserId();
        if (dto == null || dto.getUserId() == null) {
            throw new BizException("对方用户不能为空");
        }
        Long peerId = dto.getUserId();
        if (curId.equals(peerId)) {
            throw new BizException("不能拉黑自己");
        }
        // 对方账号须存在
        loadUser(peerId);
        // 已经拉黑过则不必再写
        if (userBlacklistMapper.countPair(curId, peerId) > 0) {
            throw new BizException("已经在黑名单中");
        }
        // 只能拉黑好友
        if (friendshipMapper.selectByUserIdAndFriendId(curId, peerId) <= 0) {
            throw new BizException("对方不是你的好友");
        }
        UserBlacklist row = new UserBlacklist();
        row.setUserId(curId);
        row.setBlockedUid(peerId);
        userBlacklistMapper.insert(row);
        // 从好友列表消失；Redis 关注集合由 deletePair 清理
        friendshipService.deletePair(curId, peerId);
        BlockResultVO vo = new BlockResultVO();
        vo.setUserId(peerId);
        vo.setBlocked(true);
        return vo;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 只删「我拉黑对方」这一行。不自动加回好友，对方可重新发申请。
     */
    @Override
    @Transactional
    public void unblockUser(Long userId) {
        Long curId = requireCurUserId();
        if (userId == null) {
            throw new BizException("对方用户不能为空");
        }
        if (userBlacklistMapper.deletePair(curId, userId) <= 0) {
            throw new BizException("不在黑名单中");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 昵称头像现查 Feign；单条查不到时仍返回该项，昵称用「用户」。
     */
    @Override
    public List<BlacklistItemVO> listBlacklist() {
        Long curId = requireCurUserId();
        List<UserBlacklist> rows = userBlacklistMapper.selectByUserId(curId);
        List<BlacklistItemVO> list = new ArrayList<>();
        for (UserBlacklist row : rows) {
            BlacklistItemVO vo = new BlacklistItemVO();
            vo.setUserId(row.getBlockedUid());
            vo.setBlockedAt(row.getCreatedAt());
            try {
                UserInfo user = loadUser(row.getBlockedUid());
                vo.setNickname(user.getNickname());
                vo.setAvatarUrl(user.getAvatarUrl());
            } catch (BizException e) {
                log.warn("黑名单用户资料查询失败, userId={}", row.getBlockedUid());
                vo.setNickname("用户");
            }
            list.add(vo);
        }
        return list;
    }

    /**
     * 往会话插入一名成员。私聊建会话时自己、对方各调一次。
     *
     * @param sessionId 刚插入的会话 ID
     * @param userId    成员用户 ID
     */
    private void insertMember(Long sessionId, Long userId) {
        ChatSessionMember member = new ChatSessionMember();
        member.setSessionId(sessionId);
        member.setUserId(userId);
        // 未读加减本期不做，建会话一律 0
        member.setUnreadCount(0);
        chatSessionMemberMapper.insert(member);
    }

    /**
     * 组装创建会话成功体：对方资料现查 user-service。
     *
     * @param sessionId 会话 ID
     * @param peerId    对方用户 ID
     * @return 前端会话卡片字段
     */
    private ChatSessionVO toSessionVo(Long sessionId, Long peerId) {
        UserInfo peer = loadUser(peerId);
        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionId(sessionId);
        vo.setSessionType(SESSION_TYPE_PRIVATE);
        vo.setPeerUserId(peerId);
        vo.setPeerNickname(peer.getNickname());
        vo.setPeerAvatarUrl(peer.getAvatarUrl());
        return vo;
    }

    /**
     * 当前用户必须已在该会话的成员表里，否则不能拉历史、发消息。
     *
     * @param sessionId 路径上的会话 ID
     * @param userId    当前登录用户
     */
    private void requireSessionMember(Long sessionId, Long userId) {
        if (sessionId == null) {
            throw new BizException("会话不存在");
        }
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException("会话不存在");
        }
        if (chatSessionMemberMapper.countMember(sessionId, userId) <= 0) {
            throw new BizException("无权访问该会话");
        }
    }

    /**
     * 双向拉黑都禁止建会话 / 发信。黑名单 CRUD 本期没有，表里有数据仍拦截。
     *
     * @param me        当前用户
     * @param peer      对方
     * @param forCreate {@code true} 用「无法发起会话」文案，{@code false} 用「发送失败」文案
     */
    private void assertNotBlocked(Long me, Long peer, boolean forCreate) {
        // 对方拉黑了我
        if (userBlacklistMapper.countPair(peer, me) > 0) {
            throw new BizException(forCreate ? "你已被对方拉黑，无法发起会话" : "消息发送失败，对方已将你拉黑");
        }
        // 我拉黑了对方
        if (userBlacklistMapper.countPair(me, peer) > 0) {
            throw new BizException(forCreate ? "你已将对方拉黑，无法发起会话" : "消息发送失败，对方已将你拉黑");
        }
    }

    /**
     * Mongo 文档转对外消息。自己的昵称固定为「我」，对方走 Feign，失败时显示「用户」。
     *
     * @param row       Mongo 一行
     * @param curId     当前用户，用来判断是不是自己发的
     * @param nickCache 本页对方昵称缓存，避免循环里反复 Feign
     * @return 列表 / 发送成功体
     */
    private ChatMessageVO toMessageVo(ChatMessage row, Long curId, Map<Long, String> nickCache) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setMsgId(row.getId());
        vo.setClientMsgId(row.getClientMsgId());
        vo.setSessionId(row.getSessionId());
        vo.setFromUserId(row.getFromUserId());
        vo.setMsgType(row.getMsgType());
        vo.setContent(row.getContent());
        vo.setCreatedAt(row.getCreatedAt());
        if (curId.equals(row.getFromUserId())) {
            vo.setFromNickname("我");
        } else {
            vo.setFromNickname(nickCache.computeIfAbsent(row.getFromUserId(), id -> {
                try {
                    return loadUser(id).getNickname();
                } catch (BizException e) {
                    return "用户";
                }
            }));
        }
        return vo;
    }

    /**
     * 会话表最后一条预览：图片固定「[图片]」，其它截到列宽。
     *
     * @param msgType 消息类型
     * @param content 已 trim 的正文
     * @return 写入 {@code last_msg_preview} 的短文本
     */
    private String previewOf(String msgType, String content) {
        if ("IMAGE".equals(msgType)) {
            return "[图片]";
        }
        if (content.length() > PREVIEW_MAX_LENGTH) {
            return content.substring(0, PREVIEW_MAX_LENGTH);
        }
        return content;
    }
}
