package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.PlaceOrderDTO;
import com.campus.food.campusfood.vo.OrderDetailVO;
import com.campus.food.campusfood.vo.OrderPlaceVO;
import com.campus.food.campusfood.vo.OrderSummaryVO;
import com.campus.food.campusfood.vo.PageVO;

/**
 * 订单：课设 7.6。已实现下单支付、用户查状态。
 *
 * @author yang
 * @since 2026-11-05
 */
public interface OrderService {

    /**
     * 详情页下单并 MOCK 支付，生成 {@code PAID} 订单。
     *
     * @param dto 下单入参
     * @return 订单号与实付
     */
    OrderPlaceVO placeOrder(PlaceOrderDTO dto);

    /**
     * 当前学生的订单列表。
     *
     * @param status 状态，可为 null 表示全部
     * @param page   页码，从 1
     * @param size   每页条数
     * @return 分页结果
     */
    PageVO<OrderSummaryVO> listMyOrders(String status, Integer page, Integer size);

    /**
     * 当前学生看自己的订单详情。
     *
     * @param orderId 订单 ID
     * @return 详情含明细
     */
    OrderDetailVO getMyOrderDetail(Long orderId);
}
