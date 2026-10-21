package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 注册成功后的公开信息，不含密码。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class RegisterVO {

    /** 用户 ID */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 角色 ID，注册默认为普通用户 */
    private Integer roleId;
}