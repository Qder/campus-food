package com.campus.food.campusfood.config;

import com.campus.food.campusfood.interceptor.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 MVC 拦截器。登录和注册不走 JWT，其余路径默认校验。
 *
 * @author yang
 * @since 2026-10-16
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * @param jwtAuthInterceptor JWT 拦截器
     */
    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    /**
     * 全局挂载 JWT 拦截器。登录、注册和错误页排除，避免未登录用户无法拿到令牌。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/register",
                        "/auth/login",
                        "/user/ping",
                        "/food/ping",
                        "/merchant/ping",
                        "/error"
                );
    }
}
