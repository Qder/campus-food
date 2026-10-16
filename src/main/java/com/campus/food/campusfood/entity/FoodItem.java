package com.campus.food.campusfood.entity;

import com.campus.food.campusfood.enums.FoodItemStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 美食商品，对应表 {@code food_item}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItem {

    /**
     * 美食ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 美食名称
     */
    private String name;

    /**
     * 类型标签冗余
     */
    private String foodType;

    /**
     * 口味标签
     */
    private String tasteTags;

    /**
     * 食材
     */
    private String ingredients;

    /**
     * 价格(元)
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 平均评分
     */
    private BigDecimal avgScore;

    /**
     * 评价数
     */
    private Integer reviewCount;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 推荐标签(招牌/新品等)
     */
    private String recommendTag;

    /**
     * 0草稿1上架2下架
     * {@link FoodItemStatus} 0=草稿, 1=上架, 2=下架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    private Integer deleted;

}
