package com.campus.food.campusfood.entity;

import lombok.Data;

@Data
public class OrderRequest {
    private String userId;
    private String foodId;
    private String foodName;
    private String foodPrice;
    private String foodQuantity;
    private String foodTotalPrice;
    private String foodStatus;
    private String foodCreateTime;
    private String foodUpdateTime;
}
