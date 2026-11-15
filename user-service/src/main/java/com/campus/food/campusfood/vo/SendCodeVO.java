package com.campus.food.campusfood.vo;

import lombok.Data;

/**
 * 发送验证码响应体。
 *
 * @author yang
 * @since 2026-11-15
 */
@Data
public class SendCodeVO {

    /** 验证码过期时间（秒） */
    private Integer expireIn;

    /** 多少秒后可重新发送 */
    private Integer resendAfter;
}