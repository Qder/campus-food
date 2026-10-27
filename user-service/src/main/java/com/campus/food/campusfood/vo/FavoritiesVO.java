package com.campus.food.campusfood.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 收藏列表卡片，对应 V1.3 {@code GET /user/favorites} 的 list 元素。
 *
 * @author yang
 * @since 2026-10-27
 */
@Data
public class FavoritiesVO {

    /** 收藏记录主键 */
    private Long favoriteId;

    /** 1 美食 / 2 店铺 */
    private Integer targetType;

    /** 美食或店铺 ID */
    private Long targetId;

    /** 菜名或店名；目标失效时为「已失效」 */
    private String title;

    /** 美食为「店名 · 校区」，店铺为校区 */
    private String subTitle;

    /** 美食封面或店铺 logo */
    private String coverUrl;

    /** 仅美食有价格 */
    private BigDecimal price;

    /** 仅美食有均分 */
    private BigDecimal avgScore;

    /** 目标当前状态：美食 1 上架 / 2 下架；店铺为经营状态 */
    private Integer status;

    /** 收藏时间 */
    private LocalDateTime createdAt;
}
