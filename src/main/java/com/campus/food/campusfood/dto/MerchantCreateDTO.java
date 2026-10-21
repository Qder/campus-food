package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员代建店铺的请求体，对应 {@code POST /admin/merchants}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class MerchantCreateDTO {

    /**
     * 店铺 ID。代建时不使用，由数据库生成
     */
    private Long id;

    /**
     * 店主用户ID
     */
    @NotNull(message = "店主用户ID不能为空")
    private Long ownerUserId;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 50, message = "店铺名称长度不能超过50个字符")
    private String shopName;

    /**
     * 校园区域(食堂/商圈)
     */
    @NotBlank(message = "校园区域不能为空")
    @Size(max = 50, message = "校园区域长度不能超过50个字符")
    private String campusArea;

    /**
     * 地址
     */
    @NotBlank(message = "地址不能为空")
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Size(min=11,max = 11, message = "联系电话长度必须为11个字符")
    private String contactPhone;

    /**
     * 店铺Logo
     */
    @NotBlank(message = "店铺Logo不能为空")
    private String logoUrl;

    /**
     * 店铺简介
     */
    @NotBlank(message = "店铺简介不能为空")
    @Size(max = 500, message = "店铺简介长度不能超过500个字符")
    private String description;

    /**
     * 营业执照图片URL
     */
    @NotBlank(message = "营业执照图片URL不能为空")
    @Size(max = 200, message = "营业执照图片URL长度不能超过200个字符")
    private String licenseImgUrl;
}
