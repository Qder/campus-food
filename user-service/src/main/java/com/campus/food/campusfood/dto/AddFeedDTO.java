package com.campus.food.campusfood.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布动态入参。图片接口为数组，入库时拼成逗号字符串。
 *
 * @author yang
 * @since 2026-10-31
 */
@Data
public class AddFeedDTO {

    /** 1–1000 字 */
    @NotBlank(message = "动态内容不能为空")
    @Size(max = 1000, message = "动态内容不能超过1000字")
    private String content;

    /** 可选，最多 9 张 */
    @Size(max = 9, message = "最多9张图片")
    private List<String> images;

    /** 0 仅自己可见 / 1 公开；不传默认 1 */
    private Integer privacy;
}
