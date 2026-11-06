package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 商家拒单入参。
 *
 * @author yang
 * @since 2026-11-06
 */
@Data
public class CancelOrderDTO {

    /** 取消原因，1–255 字 */
    private String reason;
}
