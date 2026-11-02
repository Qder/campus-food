package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 拉黑入参，对应 {@code POST /social/blacklist}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class BlockUserDTO {

    /** 要拉黑的用户 ID */
    private Long userId;
}
