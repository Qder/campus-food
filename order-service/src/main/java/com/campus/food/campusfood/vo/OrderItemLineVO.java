package com.campus.food.campusfood.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 订单详情里的一行明细。
 *
 * @author yang
 * @since 2026-11-06
 */
@Data
public class OrderItemLineVO {

    private Long foodId;

    private String foodName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal amount;
}
