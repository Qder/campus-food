package com.campus.food.campusfood.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 接单 / 完成 / 取消后的状态回执。
 *
 * @author yang
 * @since 2026-11-06
 */
@Data
public class OrderStatusResultVO {

    private Long orderId;

    private String status;

    private LocalDateTime acceptedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;
}
