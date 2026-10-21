package com.campus.food.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka 注册中心。控制台默认 {@code http://localhost:8761}。
 *
 * @author yang
 * @since 2026-10-21
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
