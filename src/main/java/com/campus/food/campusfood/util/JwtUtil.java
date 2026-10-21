package com.campus.food.campusfood.util;

import com.campus.food.campusfood.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析，配置项见 {@code application.yml} 的 {@code jwt} 节点。
 *
 * @author yang
 * @since 2026-10-16
 */
@Component
public class JwtUtil {

    /** 载荷中的用户 ID 声明名 */
    public static final String CLAIM_USER_ID = "userId";

    /** 载荷中的角色 ID 声明名 */
    public static final String CLAIM_ROLE_ID = "roleId";

    private final JwtProperties jwtProperties;

    /**
     * @param jwtProperties JWT 配置，密钥与过期时间从中读取
     */
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /** 根据配置中的 secret 生成 HMAC 签名密钥 */
    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 登录成功后签发访问令牌，载荷含用户 ID 与角色 ID。
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @return 已签名的 JWT
     */
    public String generateToken(Long userId, Integer roleId) {
        Date now = new Date();
        long ttlMs = jwtProperties.getExpireMinutes() * 60_000L;
        Date expiry = new Date(now.getTime() + ttlMs);

        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE_ID, roleId)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    /**
     * 验签并解析载荷。
     *
     * @param token 去掉前缀后的 JWT，不能为空
     * @return 载荷
     * @throws io.jsonwebtoken.JwtException 签名错误、过期或格式非法时
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 判断令牌是否可用。
     *
     * @param token 去掉前缀后的 JWT
     * @return 签名正确且未过期时为 true，否则为 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中读取用户 ID。
     *
     * @param token 去掉前缀后的 JWT
     * @return 用户 ID；载荷中没有该声明时为 null
     * @throws io.jsonwebtoken.JwtException 令牌无法解析时
     */
    public Long getUserId(String token) {
        Object id = parseToken(token).get(CLAIM_USER_ID);
        if (id == null) {
            return null;
        }
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        return Long.valueOf(id.toString());
    }

    /**
     * 从令牌中读取角色 ID。
     *
     * @param token 去掉前缀后的 JWT
     * @return 角色 ID；载荷中没有该声明时为 null
     * @throws io.jsonwebtoken.JwtException 令牌无法解析时
     */
    public Integer getRoleId(String token) {
        Object role = parseToken(token).get(CLAIM_ROLE_ID);
        if (role == null) {
            return null;
        }
        if (role instanceof Number) {
            return ((Number) role).intValue();
        }
        return Integer.valueOf(role.toString());
    }

    /**
     * 从 Authorization 头去掉配置的前缀，得到裸令牌。
     *
     * @param authorizationHeader 请求头原值，可为 null
     * @return 裸令牌；头为空时返回 null
     */
    public String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String prefix = jwtProperties.getPrefix();
        if (prefix != null && !prefix.isEmpty() && authorizationHeader.startsWith(prefix)) {
            return authorizationHeader.substring(prefix.length()).trim();
        }
        return authorizationHeader.trim();
    }

    /**
     * 读取配置中的请求头名称。
     *
     * @return 请求头名，默认 Authorization
     */
    public String getHeaderName() {
        return jwtProperties.getHeader();
    }
}
