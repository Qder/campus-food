package com.campus.food.campusfood.feign;

import com.campus.food.campusfood.vo.PageVO;
import com.campus.food.campusfood.vo.UserListVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    /**
     * 管理端用户分页列表。
     *
     * @param keyword 昵称 / 手机 / 邮箱，可为 null
     * @param status  0 封禁 / 1 正常，可为 null
     * @param page    页码，从 1 开始
     * @param size    每页条数
     * @return 分页结果；降级时可能为 null
     */
    @GetMapping("/users")
    PageVO<UserListVO> listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "status", required = false) Integer status,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size);
}
