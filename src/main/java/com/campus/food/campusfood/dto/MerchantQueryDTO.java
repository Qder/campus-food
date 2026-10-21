package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 店铺筛选条件，字段均可空。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class MerchantQueryDTO {

    /** 店铺名称，模糊匹配，最长 20 字 */
    @Size(max = 20, message = "商店名称长度不能超过20个字符")
    private String shopName;

    /** 地址，模糊匹配，最长 200 字 */
    @Size(max = 200, message = "商店地址长度不能超过200个字符")
    private String shopAddress;

    /** 审核状态：0 待审，1 通过，2 驳回，3 取消资格 */
    private Integer auditStatus;

    /** 经营状态：0 未开业，1 营业，2 停业 */
    private Integer status;

    /** 页码，从 1 开始；与 pageSize 同时有值时才分页 */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum;

    /** 每页条数，取值 1~100 */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize;
}
