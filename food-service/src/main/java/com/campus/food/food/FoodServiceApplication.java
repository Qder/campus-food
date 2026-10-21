package com.campus.food.food;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 美食服务入口。
 *
 * @author yang
 * @since 2026-10-21
 */
@SpringBootApplication(scanBasePackages = "com.campus.food")
@MapperScan("com.campus.food.campusfood.mapper")
@EnableFeignClients
public class FoodServiceApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FoodServiceApplication.class, args);
    }
}
