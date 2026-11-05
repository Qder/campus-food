package com.campus.food.campusfood.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 订单主表，对应 {@code food_order}。
 *
 * <p>明细在 {@link OrderItem}，支付记录在 {@link PaymentRecord}。
 *
 * @author yang
 * @since 2026-11-05
 */
@Data
public class FoodOrder {

    private Long id;

    /** 业务订单号，唯一 */
    private String orderNo;

    private Long userId;

    private Long merchantId;

    /** 商品总金额 */
    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 使用的营销活动 ID，可空 */
    private Long campaignId;

    /**
     * {@code PAID} 已支付待接单 / {@code ACCEPTED} 已接单 / {@code COMPLETED} 已完成 / {@code CANCELLED} 已取消。
     */
    private String status;

    /** 1 到店自提 / 2 配送到寝 */
    private Integer deliveryType;

    private String deliveryAddress;

    private String remark;

    private LocalDateTime paidAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private String cancelReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
