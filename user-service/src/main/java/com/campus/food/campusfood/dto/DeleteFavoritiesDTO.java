package com.campus.food.campusfood.dto;

import lombok.Data;

@Data
public class DeleteFavoritiesDTO {
    private Long targetId;
    private Integer targetType;
}
