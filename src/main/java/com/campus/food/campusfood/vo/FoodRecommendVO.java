package com.campus.food.campusfood.vo;

import lombok.Data;

@Data
public class FoodRecommendVO {

    private String recommendTags;
    private double score;
    private FoodItemVO food;
}
