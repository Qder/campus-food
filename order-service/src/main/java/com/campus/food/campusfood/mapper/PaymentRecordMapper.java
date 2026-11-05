package com.campus.food.campusfood.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.campus.food.campusfood.entity.PaymentRecord;

/**
 * {@code payment_record} Mapper。
 *
 * @author yang
 * @since 2026-11-05
 */
@Mapper
public interface PaymentRecordMapper {

    /**
     * 插入支付记录，回填 {@code id}。
     *
     * @param row 支付记录
     * @return 影响行数
     */
    int insert(PaymentRecord row);

    /**
     * 按订单 ID 查最近一条支付记录。
     *
     * @param orderId 订单 ID
     * @return 支付记录；没有返回 null
     */
    PaymentRecord selectLatestByOrderId(@Param("orderId") Long orderId);
}
