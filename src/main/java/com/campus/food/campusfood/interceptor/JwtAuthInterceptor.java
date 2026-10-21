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
 * 美食浏览类 GET 放行，其余业务接口必须带有效令牌。
 *
 * @author yang
 * @since 2026-10-16
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * @param jwtUtil JWT 解析工具
     */
    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 解析并校验令牌。公开的美食浏览 GET 直接放行，其余请求成功后写入 {@link UserContext}。
     *
     * @param request  当前请求
     * @param response 校验失败时写入 401 JSON，不进入控制器
     * @param handler  目标处理器，此处不使用
     * @return 放行返回 true，校验失败返回 false
     * @throws Exception 写出 401 响应失败时
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 美食浏览类 GET 公开；/food/recommend、POST 评价等仍须 JWT
        if (isPublicFoodBrowse(request)) {
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

    /**
     * {@inheritDoc}
     *
     * <p>无论成功失败都清理 {@link UserContext}，避免线程池复用串用户。
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        UserContext.clear();
    }

    /** 公开美食浏览：分类/列表/搜索、详情、评价列表（均为 GET） */
    private boolean isPublicFoodBrowse(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = requestPathWithoutQuery(request);
        if (uri == null) {
            return false;
        }
        return uri.matches(".*/food/categories/?$")
                || uri.matches(".*/food/list/?$")
                || uri.matches(".*/food/search/?$")
                || uri.matches(".*/food/\\d+/?$")
                || uri.matches(".*/food/\\d+/reviews/?$");
    }

    private static String requestPathWithoutQuery(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return null;
        }
        int q = uri.indexOf('?');
        return q >= 0 ? uri.substring(0, q) : uri;
    }

    /** 返回 401 JSON，不进入 Controller */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String safe = msg.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":401,\"message\":\"" + safe + "\"}");
    }
}
