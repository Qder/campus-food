package com.campus.food.campusfood.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 用户服务内部接口的 Feign 客户端。
 *
 * <p>通过 Eureka 服务名 {@code user-service} 调用，路径前缀 {@code /internal}，不经过 API Gateway。
 * 熔断或用户服务不可用时走 {@link UserFeignFallback}。
 *
 * @author yang
 * @since 2026-10-22
 */
@FeignClient(name = "user-service", path = "/internal", fallback = UserFeignFallback.class)
public interface UserFeignClient {

    /**
     * 按用户 ID 查询推荐等业务所需的精简资料。
     *
     * @param id 用户主键，不可为 null
     * @return 用户信息；降级时可能为 null，调用方需判空
     */
    @GetMapping("/users/{id}")
    UserInfo getUserInfo(@PathVariable("id") Long id);

    /**
     * 将指定用户的角色设为商家（入驻审核通过后由 merchant-service 调用）。
     *
     * @param id 用户主键，不可为 null
     */
    @PutMapping("/users/{id}/merchant-role")
    void setMerchantRole(@PathVariable("id") Long id);
}
