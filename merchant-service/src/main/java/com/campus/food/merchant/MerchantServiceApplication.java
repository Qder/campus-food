package com.campus.food.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商家服务入口。
 *
 * @author yang
 * @since 2026-10-21
 */
@SpringBootApplication(scanBasePackages = "com.campus.food")
@MapperScan("com.campus.food.campusfood.mapper")
@EnableFeignClients
public class MerchantServiceApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceApplication.class, args);
    }
}
