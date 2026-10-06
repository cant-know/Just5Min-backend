package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.config.WxProperties;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.AuthService;
import com.example.just5minbackend.util.JwtUtil;
import com.example.just5minbackend.util.PasswordUtil;
import com.example.just5minbackend.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 登录服务：
 * <ul>
 *   <li>微信登录：code → jscode2session → openid → 查/建用户 → 签发 JWT</li>
 *   <li>手机号注册 / 密码登录：phone + PBKDF2 密码哈希</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 开发调试专用 openid，与真实微信用户隔离 */
    private static final String DEV_OPENID = "dev_openid_h5_debugger";

    private final WxProperties wxProperties;
    private final RestClient wxRestClient;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LoginVO login(String code) {
        WxSession session = code2Session(code);

        User user = userMapper.selectByOpenid(session.openid());
        if (user == null) {
            user = new User();
            user.setOpenid(session.openid());
            user.setUnionid(session.unionid());
            userMapper.insert(user);
            log.info("微信新用户注册 userId={}", user.getId());
        }
        return issue(user);
    }

    @Override
    @Transactional
    public LoginVO registerByPhone(String phone, String password, String nickname) {
        if (userMapper.selectByPhone(phone) != null) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        User user = new User();
        user.setPhone(phone);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setNickname(nickname == null || nickname.isBlank() ? defaultNickname(phone) : nickname.trim());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发下唯一键兜底
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }
        log.info("手机号注册成功 userId={} phone={}", user.getId(), maskPhone(phone));
        return issue(user);
    }

    @Override
    public LoginVO loginByPassword(String phone, String password) {
        User user = userMapper.selectByPhone(phone);
        // 用户不存在与密码错误统一返回同一提示，避免手机号被枚举
        if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
            log.info("手机号密码登录失败 phone={}", maskPhone(phone));
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "账号已被禁用");
        }
        return issue(user);
    }

    @Override
    @Transactional
    public LoginVO devLogin(String nickname) {
        User user = userMapper.selectByOpenid(DEV_OPENID);
        if (user == null) {
            user = new User();
            user.setOpenid(DEV_OPENID);
            user.setNickname(nickname == null || nickname.isBlank() ? "浏览器调试用户" : nickname);
            userMapper.insert(user);
            log.info("开发调试用户已创建 userId={}", user.getId());
        }
        return issue(user);
    }

    private LoginVO issue(User user) {
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(user.getId()));
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        // selectByOpenid / selectByPhone 用的是含 avatar_url 的 authColumns，这里可安全回传
        vo.setAvatarUrl(user.getAvatarUrl());
        return vo;
    }

    private String defaultNickname(String phone) {
        return "用户" + phone.substring(phone.length() - 4);
    }

    private String maskPhone(String phone) {
        return phone.length() == 11 ? phone.substring(0, 3) + "****" + phone.substring(7) : "***";
    }

    private WxSession code2Session(String code) {
        if (wxProperties.getSecret() == null || wxProperties.getSecret().isBlank()) {
            throw new BusinessException(ResultCode.WX_ERROR, "服务端未配置微信 secret（请设置环境变量 WX_SECRET）");
        }

        String body;
        try {
            body = wxRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.weixin.qq.com")
                            .path("/sns/jscode2session")
                            .queryParam("appid", wxProperties.getAppId())
                            .queryParam("secret", wxProperties.getSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("调用微信 jscode2session 异常", e);
            throw new BusinessException(ResultCode.WX_ERROR, "微信服务暂时不可用，请稍后重试");
        }

        if (body == null || body.isBlank()) {
            throw new BusinessException(ResultCode.WX_ERROR, "微信登录返回为空");
        }

        JsonNode node;
        try {
            node = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.WX_ERROR, "微信登录返回解析失败");
        }

        int errcode = node.path("errcode").asInt(0);
        if (errcode != 0) {
            log.warn("微信登录失败: {}", body);
            throw new BusinessException(ResultCode.WX_ERROR, "微信登录失败(errcode=" + errcode + ")");
        }

        String openid = node.path("openid").asText(null);
        if (openid == null || openid.isBlank()) {
            throw new BusinessException(ResultCode.WX_ERROR, "微信登录未返回 openid");
        }
        String unionid = node.path("unionid").asText(null);
        return new WxSession(openid, unionid);
    }

    private record WxSession(String openid, String unionid) {
    }
}
