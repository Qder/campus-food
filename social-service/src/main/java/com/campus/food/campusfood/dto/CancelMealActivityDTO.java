package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 取消活动入参。{@code reason} 本期不落库，只做长度校验。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class CancelMealActivityDTO {

    /** 取消原因，可空，≤ 255 字 */
    private String reason;
}
