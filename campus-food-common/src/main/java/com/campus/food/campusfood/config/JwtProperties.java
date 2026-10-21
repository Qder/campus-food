package com.campus.food.campusfood.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置，绑定 {@code jwt.*}。
 *
 * @author yang
 * @since 2026-10-16
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HMAC 签名密钥 */
    private String secret;

    /** 令牌有效期，单位分钟 */
    private long expireMinutes = 120;

    /** 携带令牌的请求头名 */
    private String header = "Authorization";

    /** 令牌前缀，含尾部空格，解析时会去掉 */
    private String prefix = "Bearer ";
}
