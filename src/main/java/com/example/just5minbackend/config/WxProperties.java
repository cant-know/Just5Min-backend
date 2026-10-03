package com.example.just5minbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序配置（wx.*）。
 */
@Data
@ConfigurationProperties(prefix = "wx")
public class WxProperties {

    /** 小程序 appId */
    private String appId;

    /** 小程序 secret，务必通过环境变量注入 */
    private String secret;
}
