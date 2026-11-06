package com.campus.food.campusfood.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 下单成功响应。MOCK 付清后状态为 {@code PAID}。
 *
 * @author yang
 * @since 2026-11-05
 */
@Data
public class OrderPlaceVO {

    private Long orderId;

    private String orderNo;

    private String status;

    private BigDecimal payAmount;
}
