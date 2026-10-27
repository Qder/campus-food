package com.campus.food.campusfood.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家店铺，对应表 {@code merchant}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    /**
     * 商家/店铺ID
     */
    private Long id;

    /**
     * 店主用户ID
     */
    private Long ownerUserId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 校园区域(食堂/商圈)
     */
    private String campusArea;

    /**
     * 地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 店铺Logo
     */
    private String logoUrl;

    /**
     * 店铺简介
     */
    private String description;

    /**
     * 营业执照图片URL
     */
    private String licenseImgUrl;

    /**
     * 审核:0待审1通过2驳回3取消资格
     */
    private Integer auditStatus;

    /**
     * 管理员审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditedAt;

    /**
     * 审核管理员ID
     */
    private Long auditedBy;

    /**
     * 经营状态:0未开业1营业2停业
     */
    private Integer status;

    /**
     * 入驻通过时间
     */
    private LocalDateTime settledAt;

    /**
     * 申请时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    private Integer deleted;

}
