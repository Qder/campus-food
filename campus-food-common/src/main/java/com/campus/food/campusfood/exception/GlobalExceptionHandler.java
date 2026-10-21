package com.campus.food.campusfood.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 把业务异常、参数校验失败和未捕获异常收成统一的 {@code code}、{@code message}。
 *
 * @author yang
 * @since 2026-10-16
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常按可预期错误返回 400，文案使用异常消息。
     *
     * @param e 业务异常
     * @return 含 code、message 的 400 响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Map<String, Object>> handleBiz(BizException e) {
        return ResponseEntity.badRequest().body(body(400, e.getMessage()));
    }

    /**
     * 参数校验失败时只返回第一条错误，避免一次抛出整份字段清单。
     *
     * @param e 校验异常
     * @return 含 code、message 的 400 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "参数校验失败";
        return ResponseEntity.badRequest().body(body(400, msg));
    }

    /**
     * 未预期异常对外返回固定文案，细节只写日志，避免把内部信息暴露给调用方。
     *
     * @param e 未捕获异常
     * @return 含 code、message 的 500 响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.status(500).body(body(500, "服务器内部错误"));
    }

    /** 统一错误响应体：{@code { code, message }} */
    private Map<String, Object> body(int code, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", message);
        return m;
    }
}