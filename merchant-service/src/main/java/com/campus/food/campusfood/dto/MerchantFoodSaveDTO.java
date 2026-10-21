package com.campus.food.campusfood.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 店主新增或修改菜品的请求体。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class MerchantFoodSaveDTO {

    /** 美食 ID。新增时忽略，修改以路径参数为准 */
    private Long id;

    /**
     * 美食名称
     */
    private String name;

    /**
     * 价格，单位元
     */
    private BigDecimal price;
    
    /**
     * 美食库存
     */
    private Integer stock;

    /**
     * 美食描述
     */
    private String description;

    /**
     * 美食封面
     */
    private String coverUrl;

    /**
     * 美食分类ID
     */
    private Integer categoryId;

    /** 口味标签，逗号分隔 */
    private String tasteTags;

    /**
     * 食材
     */
    private String ingredients;
}
