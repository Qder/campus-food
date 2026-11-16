package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台公告，对应表 {@code platform_notice}。
 *
 * @author yang
 * @since 2026-11-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformNotice {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文
     */
    private String content;

    /**
     * 发布状态:0草稿1已发布
     */
    private Integer publishStatus;

    /**
     * 发布人用户ID
     */
    private Long publisherId;

    /**
     * 发布时间，草稿为 null
     */
    private LocalDateTime publishedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}