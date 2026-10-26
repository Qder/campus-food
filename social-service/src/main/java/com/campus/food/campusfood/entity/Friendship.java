package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 好友关系，对应表 {@code friendship}。
 *
 * <p>双向各存一行（A→B、B→A），删除好友时由 {@code deletePair} 成对删除；
 * {@code uk_friendship (user_id, friend_id)} 保证同一对关系不会重复。
 *
 * @author yang
 * @since 2026-10-23
 */
@Data
public class Friendship {

    /** 关系 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 好友用户 ID */
    private Long friendId;

    /** 备注名，可为空 */
    private String remark;

    /** 成为好友的时间 */
    private LocalDateTime createdAt;
}
