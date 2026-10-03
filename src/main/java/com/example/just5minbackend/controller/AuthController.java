package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.dto.PasswordLoginDTO;
import com.example.just5minbackend.dto.PhoneRegisterDTO;
import com.example.just5minbackend.dto.WxLoginDTO;
import com.example.just5minbackend.service.AuthService;
import com.example.just5minbackend.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录相关接口（全部免鉴权）：
 * <ul>
 *   <li>POST /api/auth/login          微信静默登录</li>
 *   <li>POST /api/auth/register       手机号注册</li>
 *   <li>POST /api/auth/password-login 手机号 + 密码登录</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody WxLoginDTO dto) {
        return Result.success(authService.login(dto.getCode()));
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody PhoneRegisterDTO dto) {
        return Result.success(authService.registerByPhone(dto.getPhone(), dto.getPassword(), dto.getNickname()));
    }

    @PostMapping("/password-login")
    public Result<LoginVO> passwordLogin(@Valid @RequestBody PasswordLoginDTO dto) {
        return Result.success(authService.loginByPassword(dto.getPhone(), dto.getPassword()));
    }
}
