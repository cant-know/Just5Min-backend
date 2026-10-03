package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.LoginVO;

public interface AuthService {

    /**
     * 使用 wx.login 的 code 完成登录（首次自动注册），返回登录态 token。
     */
    LoginVO login(String code);

    /**
     * 手机号注册：手机号唯一，密码以 PBKDF2 哈希落库；注册成功即视为登录。
     */
    LoginVO registerByPhone(String phone, String password, String nickname);

    /**
     * 手机号 + 密码登录。
     */
    LoginVO loginByPassword(String phone, String password);

    /**
     * 开发调试用登录：不校验微信，固定 openid 换 token。仅在 app.dev-login.enabled=true 时暴露。
     */
    LoginVO devLogin(String nickname);
}
