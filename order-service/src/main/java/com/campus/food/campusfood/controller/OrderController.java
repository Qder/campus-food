package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.PlaceOrderDTO;
import com.campus.food.campusfood.service.OrderService;
import com.campus.food.campusfood.vo.OrderPlaceVO;

/**
 * 订单接口，路径 {@code /order}。本期只开放下单支付。
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
     * 详情页下单并 MOCK 支付。用户从 JWT 取；菜名单价由服务端查库。
     *
     * @param dto 商品、配送、支付渠道与幂等令牌
     * @return 订单号、状态 {@code PAID}、实付金额
     */
    @PostMapping("/orders")
    public OrderPlaceVO placeOrder(@RequestBody PlaceOrderDTO dto) {
        return orderService.placeOrder(dto);
    }
}
