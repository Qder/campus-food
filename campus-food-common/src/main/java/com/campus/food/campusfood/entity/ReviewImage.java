package com.campus.food.campusfood.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价图片，对应表 {@code review_image}。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {

    /** 主键 */
    private Long id;

    /** 评价 ID */
    private Long reviewId;

    /** 图片地址，最长 512 字符 */
    private String url;

    /** 排序号，按上传顺序从 0 开始 */
    private Integer sortNo;

    /** 创建时间 */
    private LocalDateTime createdAt;
}