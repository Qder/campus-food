package com.campus.food.campusfood.exception;

/**
 * 可预期的业务错误，由 {@link GlobalExceptionHandler} 转成 HTTP 400。
 *
 * @author yang
 * @since 2026-10-16
 */
public class BizException extends RuntimeException {

    /**
     * @param message 返回给调用方的说明
     */
    public BizException(String message) {
        super(message);
    }
}