package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 驳回店铺入驻的请求体，对应 {@code PUT /admin/merchants/{id}/reject}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class MerchantRejectDTO {

    /** 驳回原因，不能为空白，最长 200 字，会写入审核备注 */
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 200, message = "拒绝原因长度不能超过200个字符")
    private String reason;
}
