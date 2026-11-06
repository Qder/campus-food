package com.campus.food.campusfood.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 订单列表卡片。
 *
 * @author yang
 * @since 2026-11-06
 */
@Data
public class OrderSummaryVO {

    private Long orderId;

    private String orderNo;

    private Long merchantId;

    private String shopName;

    private BigDecimal payAmount;

    private Integer deliveryType;

    private String status;

    private LocalDateTime createdAt;
}
