package com.campus.food.campusfood.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantFoodSaveDTO {
    
    private Long id;

    /**
     * 美食名称
     */
    private String name;

    /**
     * 美食价格
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

    private String tasteTags;

    /**
     * 食材
     */
    private String ingredients;
}
