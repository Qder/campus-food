package com.campus.food.campusfood;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园美食应用入口，扫描 {@code com.campus.food.campusfood.mapper} 下的 MyBatis 接口。
 *
 * @author yang
 * @since 2026-10-16
 */
@SpringBootApplication
@MapperScan("com.campus.food.campusfood.mapper")
public class CampusFoodApplication {

    /**
     * 启动 Spring Boot。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CampusFoodApplication.class, args);
    }

}
