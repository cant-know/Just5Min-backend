package com.example.just5minbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑资料入参。
 * 昵称与头像都是可选的：只传哪个就改哪个（至少传一个，否则 Service 抛参数错误）。
 */
@Data
public class UpdateProfileDTO {

    /** 对齐 user.nickname VARCHAR(64) */
    @Size(max = 64, message = "昵称最长 64 个字符")
    private String nickname;

    /** Base64 DataURL，如 data:image/jpeg;base64,...（格式与尺寸由 Service 校验） */
    private String avatarUrl;
}
