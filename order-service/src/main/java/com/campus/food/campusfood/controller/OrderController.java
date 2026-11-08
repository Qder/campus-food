package com.campus.food.campusfood.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.food.campusfood.dto.PlaceOrderDTO;
import com.campus.food.campusfood.service.OrderService;
import com.campus.food.campusfood.vo.OrderDetailVO;
import com.campus.food.campusfood.vo.OrderPlaceVO;
import com.campus.food.campusfood.vo.OrderSummaryVO;
import com.campus.food.campusfood.vo.PageVO;

/**
 * 订单接口，路径 {@code /order}。课设 7.6：下单支付、用户查状态。
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

    /**
     * 我的订单列表。仅当前学生。
     */
    @GetMapping("/orders")
    public PageVO<OrderSummaryVO> listMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return orderService.listMyOrders(status, page, size);
    }

    /**
     * 我的订单详情。仅下单人本人。
     */
    @GetMapping("/orders/{orderId}")
    public OrderDetailVO getMyOrderDetail(@PathVariable("orderId") Long orderId) {
        return orderService.getMyOrderDetail(orderId);
    }

    // 商家查看订单列表
    @GetMapping("/merchant/orders")
    public PageVO<OrderSummaryVO> listMerchantOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return orderService.listMerchantOrders(status, page, size);
    }

    // 商家接单
    @PostMapping("/merchant/orders/{orderId}/accept")
    public void acceptOrder(@PathVariable("orderId") Long orderId) {
        orderService.acceptOrder(orderId);
    }

    // 商家取消订单
    @PostMapping("/merchant/orders/{orderId}/cancel")
    public void cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
    }

    // 商家完成订单
    @PostMapping("/merchant/orders/{orderId}/complete")
    public void completeOrder(@PathVariable("orderId") Long orderId) {
        orderService.completeOrder(orderId);
    }
}
