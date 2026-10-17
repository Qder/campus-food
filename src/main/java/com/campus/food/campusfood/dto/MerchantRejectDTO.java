package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家拒绝原因
 */
@Data
public class MerchantRejectDTO {
    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 200, message = "拒绝原因长度不能超过200个字符")
    private String reason;
}
