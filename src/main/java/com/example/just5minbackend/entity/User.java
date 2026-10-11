package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表 user。
 */
@Data
public class User {

    private Long id;

    /** 微信 openid：微信登录用户才有，手机号注册用户为空 */
    private String openid;

    private String unionid;

    /** 手机号：手机号注册用户才有，微信登录用户为空 */
    private String phone;

    /** 密码哈希（PBKDF2），微信登录用户为空 */
    private String passwordHash;

    private String nickname;
    private String avatarUrl;

    /** 个性签名 */
    private String signature;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    private LocalDate birthday;

    /** 所在地区 */
    private String region;

    /** 目标院校 */
    private String targetSchool;

    /** 报考专业 */
    private String targetMajor;

    /** 考研年份，如 2027 */
    private Integer examYear;

    private Integer status;

    /** 积分余额：每提交一次答案 +1，兑换商品扣减 */
    private Integer points;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
