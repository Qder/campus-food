package com.campus.food.campusfood.dto;

import lombok.Data;

/**
 * 管理员用户列表查询，对应 {@code GET /admin/users}。
 *
 * @author yang
 * @since 2026-11-11
 */
@Data
public class UserListQueryDTO {

    /** 昵称 / 手机号 / 邮箱模糊匹配，空表示不限 */
    private String keyword;

    /** 页码，从 1 开始；空或小于 1 时按 1 */
    private Integer page;

    /** 每页条数；空或小于 1 时按 10 */
    private Integer size;

    /** 账号状态：0 封禁，1 正常；空表示不限 */
    private Integer status;
}
