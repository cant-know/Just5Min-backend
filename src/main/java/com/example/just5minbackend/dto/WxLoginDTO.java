package com.example.just5minbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录入参。
 */
@Data
public class WxLoginDTO {

    @NotBlank(message = "code 不能为空")
    private String code;
}
