package com.example.just5minbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手机号注册入参。
 */
@Data
public class PhoneRegisterDTO {

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码长度需为 6~32 位")
    private String password;

    /** 可选昵称，不填则由后端按手机号后四位生成 */
    @Size(max = 32, message = "昵称最长 32 个字符")
    private String nickname;
}
