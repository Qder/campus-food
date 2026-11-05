package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.service.OrderService;

/**
 * 订单接口，路径 {@code /order}。课设 7.6：下单支付、商家处理、用户查状态。
 *
 * @author yang
 * @since 2026-11-05
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

}
