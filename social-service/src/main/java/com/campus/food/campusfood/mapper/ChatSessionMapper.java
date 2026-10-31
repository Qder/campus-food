package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.ChatSession;

/**
 * {@code chat_session} Mapper。私聊会话一行；成员在 {@code chat_session_member}。
 *
 * @author yang
 * @since 2026-10-31
 */
@Mapper
public interface ChatSessionMapper {

    /**
     * 新建会话，回填自增 {@code id}。
     *
     * @param row 须带 {@code sessionType}；私聊再带 {@code peerUserId}
     * @return 影响行数
     */
    int insert(ChatSession row);

    /**
     * 按主键取会话。
     *
     * @param id 会话 ID
     * @return 没有返回 null
     */
    ChatSession selectById(@Param("id") Long id);

    /**
     * 两人是否已有 {@code session_type=1} 的私聊：两边都在成员表里即视为同一场，不看 {@code peer_user_id}。
     *
     * @param userId     当前用户
     * @param peerUserId 对方
     * @return 已有会话；没有返回 null
     */
    ChatSession selectPrivateBetween(@Param("userId") Long userId, @Param("peerUserId") Long peerUserId);

    /**
     * 发信成功后刷新列表预览。
     *
     * @param id        会话 ID
     * @param preview   摘要（文字截断或「[图片]」）
     * @param lastMsgAt 该条发送时间
     * @return 影响行数
     */
    int updateLastMessage(@Param("id") Long id,
                          @Param("preview") String preview,
                          @Param("lastMsgAt") java.time.LocalDateTime lastMsgAt);
}
