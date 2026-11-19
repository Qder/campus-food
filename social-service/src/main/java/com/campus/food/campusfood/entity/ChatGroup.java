package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 群聊，对应表 {@code chat_group}。
 *
 * <p>一场群聊一行，与 {@code chat_session} 通过 {@code group_id} 关联；
 * 成员关系在 {@code chat_group_member}。
 *
 * @author yang
 * @since 2026-11-19
 */
@Data
public class ChatGroup {

    private Long id;

    /** 群名称，最长 128 字 */
    private String name;

    /** 群主用户 ID */
    private Long ownerUserId;

    /** 群头像 URL，可空 */
    private String avatarUrl;

    /** 群公告，可空；本期只建群不维护 */
    private String notice;

    /** 人数上限，含群主 */
    private Integer maxMembers;

    /** 1 正常 / 0 解散；本期只写 1 */
    private Integer status;

    private LocalDateTime createdAt;
}
