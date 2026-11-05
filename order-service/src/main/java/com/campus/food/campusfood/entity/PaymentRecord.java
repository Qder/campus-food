package com.campus.food.campusfood.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 支付记录，对应 {@code payment_record}。
 *
 * @author yang
 * @since 2026-11-05
 */
@Data
public class PaymentRecord {

    private Long id;

    private Long orderId;

    private String orderNo;

    /** MOCK / WECHAT / ALIPAY */
    private String payChannel;

    private String tradeNo;

    private BigDecimal amount;

    /** 0 待支付 / 1 成功 / 2 失败 / 3 退款 */
    private Integer status;

    private String callbackRaw;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
}
