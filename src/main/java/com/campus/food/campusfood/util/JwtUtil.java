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

/** JWT 签发与解析，配置项见 {@code application.yml} 的 {@code jwt} 节点 */
@Component
public class JwtUtil {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE_ID = "roleId";

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /** 根据配置中的 secret 生成 HMAC 签名密钥 */
    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 登录成功后签发 access token，载荷含 userId、roleId */
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

    /** 验签并解析 payload，失败抛异常 */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 签名正确且未过期返回 true */
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

    /** 从 token 中读取 userId */
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

    /** 从 token 中读取 roleId */
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

    /** 从 Authorization 头去掉 Bearer 前缀，得到裸 token */
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

    /** 配置中的请求头名称，一般为 Authorization */
    public String getHeaderName() {
        return jwtProperties.getHeader();
    }
}
