package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 群成员，对应表 {@code chat_group_member}。
 *
 * <p>一个群一人一行，唯一键 {@code (group_id, user_id)}；
 * 建群时群主写一行，其余成员各写一行。
 *
 * @author yang
 * @since 2026-11-19
 */
@Data
public class ChatGroupMember {

    private Long id;

    /** 所属群，对应 {@code chat_group.id} */
    private Long groupId;

    /** 成员用户 ID */
    private Long userId;

    /** 0 成员 / 1 管理员 / 2 群主；本期只写 0 和 2 */
    private Integer role;

    /** 群内昵称，可空；本期不设置 */
    private String nicknameInGroup;

    private LocalDateTime joinedAt;
}
