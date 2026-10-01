package com.box.utils;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class QWeatherJwtUtil {

    @Value("${qweather.kid}")
    private String kid;

    @Value("${qweather.sub}")
    private String sub;

    @Value("${qweather.private-key-path}")
    private String privateKeyPath;

    @Value("${qweather.iss}")
    private String iss;

    public String generateToken() {
        try {
            String pem = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
            String base64 = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("Ed25519");
            PrivateKey privateKey = kf.generatePrivate(spec);

            String token = Jwts.builder()
                    .header().keyId(kid).and()
                    .issuer(iss)
                    .subject(sub)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600 * 1000))
                    .signWith(privateKey, Jwts.SIG.EdDSA)
                    .compact();


            System.out.println("JWT13131:" + token);

            return token;
        } catch (Exception e) {
            throw new RuntimeException("生成天气JWT失败", e);
        }
    }
}