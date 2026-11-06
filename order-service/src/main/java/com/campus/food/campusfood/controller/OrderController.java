package com.campus.food.campusfood.controller;

import javax.naming.spi.DirStateFactory.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.entity.OrderRequest;
import com.campus.food.campusfood.service.OrderService;
import com.campus.food.campusfood.vo.OrderPlaceVO;

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

    /**
     * 下单接口。
     *
     * @param orderRequest 订单请求
     * @return 订单响应
     */
    @PostMapping("/orders")
    public OrderPlaceVO placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest);
    }
}
