package com.campus.food.campusfood.dto;

import lombok.Data;

@Data
public class AddFavoritiesDTO {
    private Long targetId;
    private Integer targetType;
}
