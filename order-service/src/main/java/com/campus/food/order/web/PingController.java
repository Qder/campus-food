package com.campus.food.order.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 骨架探活，确认 Gateway 能打到本服务。
 *
 * @author yang
 * @since 2026-11-05
 */
@RestController
@RequestMapping("/order")
public class PingController {

    /**
     * @return 服务名
     */
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("service", "order-service");
    }
}
