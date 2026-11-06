package com.campus.food.campusfood.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campus.food.campusfood.mapper.FoodOrderMapper;
import com.campus.food.campusfood.mapper.OrderItemMapper;
import com.campus.food.campusfood.mapper.PaymentRecordMapper;
import com.campus.food.campusfood.service.OrderService;

/**
 * 订单业务实现。
 *
 * @author yang
 * @since 2026-11-05
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private FoodOrderMapper foodOrderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    /**
     * @param foodOrderMapper     订单主表
     * @param orderItemMapper     订单明细
     * @param paymentRecordMapper 支付记录
     */
    @Autowired
    public OrderServiceImpl(FoodOrderMapper foodOrderMapper,
            OrderItemMapper orderItemMapper,
            PaymentRecordMapper paymentRecordMapper) {
        this.foodOrderMapper = foodOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }
}
