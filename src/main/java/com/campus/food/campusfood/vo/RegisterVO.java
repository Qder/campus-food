package com.campus.food.campusfood.vo;

import lombok.Data;

/** 注册成功返回，不含密码等敏感字段 */
@Data
public class RegisterVO {
    private Long id;
    private String nickname;
    private Integer roleId;
}