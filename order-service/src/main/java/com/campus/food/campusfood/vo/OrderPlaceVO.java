package com.campus.food.campusfood.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderPlaceVO {
    private Long orderId;
    private String orderNo;
    private Integer orderStatus;
    private BigDecimal amount;
}
