package com.campus.food.campusfood.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加/取消收藏的成功体，对应 V1.3 {@code POST/DELETE /user/favorites}。
 *
 * @author yang
 * @since 2026-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteAddVO {

    /** 1 美食 / 2 店铺 */
    private Integer targetType;

    /** 美食或店铺 ID */
    private Long targetId;

    /** 添加后为 true，取消后为 false */
    private Boolean favorited;
}
