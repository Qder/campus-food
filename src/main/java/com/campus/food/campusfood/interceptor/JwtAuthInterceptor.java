package com.campus.food.campusfood.interceptor;

import com.campus.food.campusfood.common.UserContext;
import com.campus.food.campusfood.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET /food/{id}/reviews 公开浏览；POST 同路径仍需登录（不在此放行）
        if (isPublicFoodReviewList(request)) {
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
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        UserContext.clear();
    }

    /** 是否为公开评价列表：GET 且路径形如 /food/数字/reviews */
    private boolean isPublicFoodReviewList(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        int q = uri.indexOf('?');
        if (q >= 0) {
            uri = uri.substring(0, q);
        }
        return uri.matches(".*/food/\\d+/reviews/?$");
    }

    /** 返回 401 JSON，不进入 Controller */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String safe = msg.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":401,\"message\":\"" + safe + "\"}");
    }
}
