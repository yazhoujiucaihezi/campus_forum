package com.box.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

/**
 * JWT 工具类：负责令牌的验签与解析
 */
@Component
public class JwtUtils {

    /** 签名密钥，需与登录时签发令牌使用的密钥保持一致 */
    private static final String SECRET_KEY = "box-forum-secret-key-1234567890abcdef";

    /**
     * 生成令牌（预留方法，当前登录逻辑在 AuthServiceImpl 中直接签发）
     */
    public static String generateToken(String username,String role) {
        return null;
    }

    /**
     * 验证并解析令牌
     * 验签失败或令牌过期会抛出 JWTVerificationException
     *
     * @param token 待验证的 JWT 字符串
     * @return 解析后的令牌对象，可从中读取 username、role 等声明
     */
    public static DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY)).build().verify(token);
    }

    public static Integer getUid(String authHeader) {
        String token = authHeader.substring(7);
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("uid").asInt();
    }

    public static String getRole(String authHeader) {
        String token = authHeader.substring(7);
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("role").asString();
    }
}
