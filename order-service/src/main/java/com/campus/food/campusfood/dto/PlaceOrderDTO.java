package com.campus.food.campusfood.dto;

import java.util.List;

import lombok.Data;

/**
 * 详情页下单入参，对应 {@code POST /order/orders}。
 *
 * <p>用户 ID 从 JWT 取，不传。菜名、单价由服务端查 {@code food_item} 并算价。
 *
 * @author yang
 * @since 2026-11-05
 */
@Data
public class PlaceOrderDTO {

    /** 至少 1 条；须同一店铺 */
    private List<OrderLineDTO> items;

    /** 1 到店自提 / 2 配送到寝 */
    private Integer deliveryType;

    /** {@code deliveryType=2} 时必填，≤ 255 字 */
    private String deliveryAddress;

    /** 用户备注，可空，≤ 255 字 */
    private String remark;

    /** 支付渠道：默认 {@code WECHAT}；也可传 {@code MOCK} */
    private String payChannel;

    /** 前端生成的幂等令牌，防重复提交 */
    private String clientOrderToken;
}
