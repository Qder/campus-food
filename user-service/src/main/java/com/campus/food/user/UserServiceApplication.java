package com.campus.food.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务入口。当前仅骨架，业务仍在单体 {@code src/}。
 *
 * @author yang
 * @since 2026-10-21
 */
@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
