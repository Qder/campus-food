package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 好友动态列表项，对应 {@code GET /social/feeds} 分页里的一个元素。
 *
 * <p>在「我的动态」字段基础上多了作者 {@code userId} / {@code nickname} / {@code avatarUrl}；
 * 不跨模块复用 user-service 的 FeedVO，避免服务间 VO 耦合。
 *
 * @author yang
 * @since 2026-11-19
 */
@Data
public class FriendFeedVO {

    /** 动态 ID */
    private Long feedId;

    /** 发布者用户 ID */
    private Long userId;

    /** 发布者昵称，Feign 降级时为「用户」 */
    private String nickname;

    /** 发布者头像 URL，可为 null */
    private String avatarUrl;

    /** 动态文字内容 */
    private String content;

    /** 图片列表，逗号串拆分；无图为空列表 */
    private List<String> images;

    /** 隐私：0 仅自己 / 1 公开，好友动态必然为 1 */
    private Integer privacy;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数，现网固定 0 */
    private Integer commentCount;

    /** 发布时间 */
    private LocalDateTime createdAt;
}
