package com.campus.food.campusfood.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 订单详情。
 *
 * @author yang
 * @since 2026-11-06
 */
@Data
public class OrderDetailVO {

    private Long orderId;

    private String orderNo;

    private Long merchantId;

    private String shopName;

    private List<OrderItemLineVO> items;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer deliveryType;

    private String deliveryAddress;

    private String remark;

    private String status;

    private String cancelReason;

    private LocalDateTime paidAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
}
