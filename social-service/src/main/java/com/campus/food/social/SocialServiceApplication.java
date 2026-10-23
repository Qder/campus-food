package com.campus.food.social;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 社交服务入口：好友、Feed 等（骨架阶段仅探活接口）。
 *
 * @author yang
 * @since 2026-10-23
 */
@SpringBootApplication(scanBasePackages = "com.campus.food")
@MapperScan({"com.campus.food.campusfood.mapper", "com.campus.food.social.mapper"})
@EnableFeignClients(basePackages = "com.campus.food.campusfood.feign")
public class SocialServiceApplication {

    /**
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }
}
