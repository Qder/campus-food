package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 管理员用户列表项，对应 {@code GET /admin/users}。
 *
 * @author yang
 * @since 2026-11-11
 */
@Data
public class UserListVO {

    /** 用户主键 */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色：1 学生，2 商家，3 管理员 */
    private Integer roleId;

    /** 账号状态：0 封禁，1 正常 */
    private Integer status;
}
