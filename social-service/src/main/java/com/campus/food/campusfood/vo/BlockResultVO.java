package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 拉黑成功体：{@code userId} + {@code blocked=true}。
 *
 * @author yang
 * @since 2026-11-02
 */
@Data
public class BlockResultVO {

    private Long userId;

    private Boolean blocked;
}
