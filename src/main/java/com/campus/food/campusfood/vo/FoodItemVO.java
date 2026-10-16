package com.campus.food.campusfood.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 上架美食的列表/详情视图，补充店铺名、校区等展示用冗余字段。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class FoodItemVO {

    /** 美食 ID */
    private Long id;

    /** 商家 ID */
    private Long merchantId;

    /** 店铺名 */
    private String shopName;

    /** 校区/区域 */
    private String campusArea;

    /** 分类 ID */
    private Integer categoryId;

    /** 美食名称 */
    private String name;

    /** 类型标签 */
    private String foodType;

    /** 价格（元） */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 描述 */
    private String description;

    /** 封面图 URL */
    private String coverUrl;

    /** 平均评分 */
    private BigDecimal avgScore;

    /** 评价数 */
    private Integer reviewCount;

    /** 销量 */
    private Integer salesCount;

    /** 推荐标签（招牌/新品等） */
    private String recommendTag;

    /** 状态码，取值见 {@link com.campus.food.campusfood.enums.FoodItemStatus} */
    private Integer status;

    /** 推荐计算用，列表/详情接口通常不返回 */
    private String tasteTags;

    /** 推荐忌口过滤用，列表/详情接口通常不返回 */
    private String ingredients;
}
