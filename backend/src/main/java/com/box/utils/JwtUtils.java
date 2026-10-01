package com.box.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "box-forum-secret-key-1234567890abcdef";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(String username, String role) {
        return null;
    }

    public static Claims parse(String token) {
        return Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
    }

    public static Integer getUid(String authHeader) {
        String token = authHeader.substring(7);
        return parse(token).get("uid", Integer.class);
    }

    public static String getRole(String authHeader) {
        String token = authHeader.substring(7);
        return parse(token).get("role", String.class);
    }
}