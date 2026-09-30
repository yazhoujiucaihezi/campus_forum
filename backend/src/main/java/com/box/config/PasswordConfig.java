package com.box.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码相关配置：注册 BCrypt 编码器，供登录时比对密码哈希使用
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 密码编码器（加密 + 哈希比对）
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

