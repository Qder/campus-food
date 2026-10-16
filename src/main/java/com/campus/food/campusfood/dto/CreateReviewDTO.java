package com.campus.food.campusfood.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评价，对应 {@code POST /food/{id}/reviews}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class CreateReviewDTO {

    /** 评分，取值 1~5，不能为空 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分需在 1~5 之间")
    @Max(value = 5, message = "评分需在 1~5 之间")
    private Integer score;

    /** 评价内容，最长 2000 字，可为空 */
    @Size(max = 2000, message = "评价内容过长")
    private String content;
}
