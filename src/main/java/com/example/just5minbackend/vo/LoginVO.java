package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 登录返回。
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String nickname;

    /** 手机号注册/密码登录时返回，微信登录用户为 null */
    private String phone;

    /** 头像（Base64 DataURL），未设置时为 null */
    private String avatarUrl;
}
