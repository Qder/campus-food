package com.campus.food.campusfood.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户处罚记录，对应表 {@code user_punishment}。
 *
 * @author yang
 * @since 2026-11-14
 */
@Data
public class UserPunishment {

    /** 主键 */
    private Long id;

    /** 被处罚用户 ID */
    private Long userId;

    /** 处罚动作：WARN / LIMIT / BAN */
    private String action;

    /** 处罚原因 */
    private String reason;

    /** 操作管理员 ID */
    private Long operatorId;

    /** 到期时间，null 表示永久 */
    private LocalDateTime expireAt;

    /** 处罚时间 */
    private LocalDateTime createdAt;
}
