package com.campus.food.campusfood.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.OrderItem;

/**
 * {@code order_item} Mapper。
 *
 * @author yang
 * @since 2026-11-05
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 插入一条明细。
     *
     * @param row 明细
     * @return 影响行数
     */
    int insert(OrderItem row);

    /**
     * 按订单 ID 查全部明细。
     *
     * @param orderId 订单 ID
     * @return 明细列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
