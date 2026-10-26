package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 好友申请，对应表 {@code friend_request}。
 *
 * <p>一行记录即一次「A 申请加 B 为好友」，处理结果记在 {@link #status}；
 * 同意后由业务侧写入 {@link Friendship} 的双向关系。
 *
 * @author yang
 * @since 2026-10-23
 */
@Data
public class FriendRequest {

    /** 申请 ID */
    private Long id;

    /** 申请人用户 ID */
    private Long fromUserId;

    /** 被申请人用户 ID */
    private Long toUserId;

    /** 申请留言，可为空 */
    private String message;

    /** 状态：0 待处理，1 已同意，2 已拒绝，3 已过期 */
    private Integer status;

    /** 申请时间 */
    private LocalDateTime createdAt;

    /** 处理时间，未处理时为空 */
    private LocalDateTime handledAt;
}
