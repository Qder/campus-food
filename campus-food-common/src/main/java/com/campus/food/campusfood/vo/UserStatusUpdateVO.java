package com.campus.food.campusfood.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封禁 / 恢复用户的内网调用结果，用于把业务失败文案带回调用方。
 *
 * @author yang
 * @since 2026-11-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateVO {

    /** 是否成功 */
    private boolean success;

    /** 失败原因，成功时为 null */
    private String message;

    /**
     * 成功结果。
     *
     * @return {@code success=true} 的结果
     */
    public static UserStatusUpdateVO ok() {
        return new UserStatusUpdateVO(true, null);
    }

    /**
     * 失败结果。
     *
     * @param message 失败原因
     * @return {@code success=false} 的结果
     */
    public static UserStatusUpdateVO fail(String message) {
        return new UserStatusUpdateVO(false, message);
    }
}
