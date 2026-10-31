package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 动态列表卡片。图片由库内逗号字符串拆成数组。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class FeedVO {

    /** 动态主键 */
    private Long feedId;

    private String content;

    /** 封面图 URL 列表；无图为空列表 */
    private List<String> images;

    /** 0 仅自己可见 / 1 公开 */
    private Integer privacy;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数，固定 0 */
    private Integer commentCount;

    private LocalDateTime createdAt;
}
