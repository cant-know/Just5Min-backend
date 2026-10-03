package com.example.just5minbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置（jwt.*）。
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥，生产环境必须为高熵随机串 */
    private String secret;

    /** 过期时间（小时） */
    private long expireHours = 168;
}
