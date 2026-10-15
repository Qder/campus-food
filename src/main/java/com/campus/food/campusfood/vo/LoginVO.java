package com.campus.food.campusfood.vo;

import lombok.Data;

@Data
public class LoginVO {

    private Long id;
    private String nickname;
    private Integer roleId;
    
    /** JWT 等令牌，接入鉴权后赋值 */
    private String token;
}
