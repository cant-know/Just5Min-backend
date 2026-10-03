package com.example.just5minbackend.dto;

import lombok.Data;

/**
 * 开发调试登录入参（可空）。
 */
@Data
public class DevLoginDTO {

    /** 可选，调试用户昵称 */
    private String nickname;
}
