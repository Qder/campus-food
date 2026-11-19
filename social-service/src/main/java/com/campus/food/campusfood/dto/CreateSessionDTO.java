package com.campus.food.campusfood.dto;

import java.util.List;

import lombok.Data;

/**
 * 创建会话入参：私聊传对方用户，群聊传群名称与成员。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class CreateSessionDTO {

    /** 对方用户 ID，仅私聊使用 */
    private Long peerUserId;

    /** 1 私聊 / 2 群聊 */
    private Integer sessionType;

    /** 群名称，仅群聊使用；去空白后必填，最长 128 字 */
    private String title;

    /** 群成员用户 ID，不含创建者；仅群聊使用，至少 1 人 */
    private List<Long> memberIds;
}
