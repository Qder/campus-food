package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 黑名单列表一项。昵称头像现查 user-service，不存在黑名单表里。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class BlacklistItemVO {

    private Long userId;

    private String nickname;

    private String avatarUrl;

    /** 拉黑时间，对应 {@code user_blacklist.created_at} */
    private LocalDateTime blockedAt;
}
