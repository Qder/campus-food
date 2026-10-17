package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantQueryDTO {
    @Size(max = 20, message = "商店名称长度不能超过20个字符")
    private String shopName;
    @Size(max = 200, message = "商店地址长度不能超过200个字符")
    private String shopAddress;
    /** 审核状态：0待审 1通过 2驳回 3取消资格 */
    private Integer auditStatus;
    /** 经营状态：0未开业 1营业 2停业 */
    private Integer status;
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer pageSize;
}
