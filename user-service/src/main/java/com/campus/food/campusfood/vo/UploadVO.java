package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 图片上传结果。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
public class UploadVO {

    /** 可直接用于 img src 的相对地址，形如 /uploads/avatar/2026/11/15/xxx.png */
    private String url;

    /** 文件大小，单位字节 */
    private long size;
}
