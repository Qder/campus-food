package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员封禁 / 恢复用户的请求体，对应 {@code PUT /admin/users/{id}/status}。
 *
 * @author yang
 * @since 2026-11-14
 */
@Data
public class UserStatusUpdateDTO {

    /** 目标状态：0 封禁，1 正常（恢复） */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;

    /** 封禁原因，封禁时必填，最长 512 字；恢复时可为空 */
    @Size(max = 512, message = "封禁原因不能超过512个字符")
    private String reason;
}
