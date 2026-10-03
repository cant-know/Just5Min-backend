package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.dto.DevLoginDTO;
import com.example.just5minbackend.service.AuthService;
import com.example.just5minbackend.vo.LoginVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开发调试登录：浏览器/H5 没有 wx.login，用它换 token 才能联调。
 * <p>仅当 {@code app.dev-login.enabled=true} 时才注册，关闭状态下该接口根本不存在（404）。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.dev-login.enabled", havingValue = "true")
public class DevAuthController {

    private final AuthService authService;

    @PostConstruct
    void warnEnabled() {
        log.warn("开发调试登录接口已开启：POST /api/auth/dev-login（上线前请设 app.dev-login.enabled=false）");
    }

    @PostMapping("/dev-login")
    public Result<LoginVO> devLogin(@RequestBody(required = false) DevLoginDTO dto) {
        String nickname = dto == null ? null : dto.getNickname();
        return Result.success(authService.devLogin(nickname));
    }
}
