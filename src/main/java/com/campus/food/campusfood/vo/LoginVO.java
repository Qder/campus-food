package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 登录成功响应，含访问令牌。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
public class LoginVO {

    /** 用户 ID */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 角色 ID */
    private Integer roleId;

    /** 访问令牌，放在后续请求的 Authorization 头中 */
    private String token;
}
