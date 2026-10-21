package com.campus.food.campusfood.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全相关 Bean。当前只提供密码编码器，接口鉴权由 JWT 拦截器负责。
 *
 * @author yang
 * @since 2026-10-16
 */
@Configuration
public class SecurityConfig {

    /**
     * 注册与登录共用的 BCrypt 编码器。
     *
     * @return 密码编码器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}