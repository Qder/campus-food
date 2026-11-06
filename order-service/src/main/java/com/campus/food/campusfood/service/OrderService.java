package com.campus.food.campusfood.service;

import com.campus.food.campusfood.dto.PlaceOrderDTO;
import com.campus.food.campusfood.vo.OrderPlaceVO;

/**
 * 订单：课设 7.6。本期只实现「用户下单并 MOCK 支付」。
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
}
