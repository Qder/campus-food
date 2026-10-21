package com.campus.food.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API 网关。对外端口 8080，与现有前端约定一致。
 *
 * <p>不要与单体 {@code CampusFoodApplication} 同时占用 8080。
 *
 * @author yang
 * @since 2026-10-21
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
