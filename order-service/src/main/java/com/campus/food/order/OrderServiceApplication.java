package com.campus.food.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务入口：下单、支付、商家接单（课设 7.6）。
 *
 * @author yang
 * @since 2026-11-05
 */
@SpringBootApplication(scanBasePackages = "com.campus.food")
@MapperScan("com.campus.food.campusfood.mapper")
@EnableFeignClients(basePackages = "com.campus.food.campusfood.feign")
public class OrderServiceApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
