package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 好友申请响应，对应 {@code friend_request} 的一行。
 *
 * <p>发送、同意、拒绝三个动作都返回处理后的最新状态，前端不必再查一次列表。
 *
 * @author yang
 * @since 2026-10-23
 */
@Data
public class FriendRequestVo {

    /** 申请 ID，同意与拒绝时用它定位 */
    private Long id;

    /** 申请人用户 ID */
    private Long fromUserId;

    /** 申请人昵称，收件箱展示用；用户服务不可用时为空 */
    private String fromNickname;

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
