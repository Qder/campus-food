package com.campus.food.campusfood.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务不可用时的降级：查询当作用户不存在，升角色先跳过。
 *
 * @author yang
 * @since 2026-10-22
 */
@Component
public class UserFeignFallback implements UserFeignClient {

    private static final Logger log = LoggerFactory.getLogger(UserFeignFallback.class);

    @Override
    public UserInfo getUserInfo(Long id) {
        log.warn("用户服务不可用，getUserInfo 降级, id={}", id);
        return null;
    }

    @Override
    public void setMerchantRole(Long id) {
        log.warn("用户服务不可用，setMerchantRole 跳过, id={}", id);
    }
}
