package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评价回复，对应 {@code POST /food/reviews/{id}/replies}。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
public class CreateReplyDTO {

    /** 回复内容，去空白后不能为空，最长 1000 字 */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容过长")
    private String content;
}