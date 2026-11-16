package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员发布公告 / 存草稿的请求体，对应 {@code POST /admin/notices}。
 *
 * @author yang
 * @since 2026-11-16
 */
@Data
public class NoticeDTO {

    /** 标题，最长 128 字 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不能超过128个字符")
    private String title;

    /** 正文 */
    @NotBlank(message = "正文不能为空")
    private String content;

    /** 发布状态：0 草稿，1 已发布；省略按 0 草稿处理 */
    @Min(value = 0, message = "发布状态只能是 0 或 1")
    @Max(value = 1, message = "发布状态只能是 0 或 1")
    private Integer publishStatus;
}