package com.campus.food.campusfood.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 订单明细，对应 {@code order_item}。菜名与单价为下单快照。
 *
 * @author yang
 * @since 2026-11-05
 */
@Data
public class OrderItem {

    private Long id;

    private Long orderId;

    private Long foodId;

    private String foodName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal amount;
}
