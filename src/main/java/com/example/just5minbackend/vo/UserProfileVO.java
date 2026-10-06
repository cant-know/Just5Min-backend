package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 「我的资料」返回。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private Long userId;

    /** 昵称为空时会由后端兜底成「用户{id}」，前端可直接展示 */
    private String nickname;

    /** 手机号；微信登录用户为 null */
    private String phone;

    /** 头像（Base64 DataURL），未设置为 null */
    private String avatarUrl;

    /** 积分余额 */
    private Integer points;
}
