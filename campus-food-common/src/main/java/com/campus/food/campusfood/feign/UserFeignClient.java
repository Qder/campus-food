package com.campus.food.campusfood.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

//跨服务调用用户服务
@FeignClient(name = "user-service", path = "/internal",fallback = UserFeignFallback.class)
public interface UserFeignClient {

    //通过id查找用户信息
    @GetMapping("/users/{id}")
    UserInfo getUserInfo(@PathVariable("id") Long id);

    //通过id设置用户角色为商户
    @PutMapping("/users/{id}/merchant-role")
    void setMerchantRole(@PathVariable("id") Long id);
}
