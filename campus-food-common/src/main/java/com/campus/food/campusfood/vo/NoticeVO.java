package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 平台公告展示项，管理端与用户端共用。
 *
 * @author yang
 * @since 2026-11-16
 */
@Data
public class NoticeVO {

    /** 公告 ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 发布状态：0 草稿，1 已发布 */
    private Integer publishStatus;

    /** 发布时间，草稿为 null */
    private LocalDateTime publishedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}