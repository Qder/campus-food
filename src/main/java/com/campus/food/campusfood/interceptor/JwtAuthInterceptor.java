package com.campus.food.campusfood.interceptor;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 校验请求头中的 JWT，并将用户写入 {@link UserContext}。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 解析并校验 Token，成功则写入 {@link UserContext}。
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String headerValue = request.getHeader(jwtUtil.getHeaderName());
        String token = jwtUtil.resolveToken(headerValue);
        if (token == null) {
            writeUnauthorized(response, "未登录或 Token 缺失");
            return false;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorized(response, "Token 无效或已过期");
                return false;
            }
            Long userId = jwtUtil.getUserId(token);
            Integer roleId = jwtUtil.getRoleId(token);
            if (userId == null) {
                writeUnauthorized(response, "Token 无效或已过期");
                return false;
            }
            UserContext.set(userId, roleId);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, "Token 无效或已过期");
            return false;
        }
    }

    /** 请求完成后清理 {@link UserContext} */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 返回 401 JSON，不进入 Controller */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String safe = msg.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":401,\"message\":\"" + safe + "\"}");
    }
}
