package com.campus.food.campusfood.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserInfo
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private Long id;
    private String nickname;
    private String tasteTags;
    private String dislikeIngredients;
    private int deleted;
    private int roleId;
}
