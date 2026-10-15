package com.campus.food.campusfood;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.food.campusfood.mapper")
public class CampusFoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusFoodApplication.class, args);
    }

}
