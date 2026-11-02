package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户黑名单一行，对应表 {@code user_blacklist}。
 *
 * <p>{@code userId} 拉黑 {@code blockedUid}。唯一键 {@code uk_blacklist (user_id, blocked_uid)}，
 * 同一对不会重复。建会话、发消息、加好友都会查这张表。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class UserBlacklist {

    private Long id;

    /** 拉黑人 */
    private Long userId;

    /** 被拉黑人 */
    private Long blockedUid;

    /** 拉黑时间，对应列 {@code created_at} */
    private LocalDateTime createdAt;
}
