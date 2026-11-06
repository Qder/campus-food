package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 下单明细一行，对应写入 {@code order_item} 前的入参。
 *
 * @author yang
 * @since 2026-11-05
 */
@Data
public class OrderLineDTO {

    /** 美食 ID，← {@code food_item.id} */
    private Long foodId;

    /** 数量，1–99 */
    private Integer quantity;
}
