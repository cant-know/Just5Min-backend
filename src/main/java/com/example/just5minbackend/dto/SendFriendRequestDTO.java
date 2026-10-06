package com.example.just5minbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送好友请求入参。
 */
@Data
public class SendFriendRequestDTO {

    @NotNull(message = "请选择要添加的用户")
    private Long toUserId;

    /** 验证附言（可选），对齐 friend_request.message VARCHAR(64) */
    @Size(max = 64, message = "验证附言最长 64 个字符")
    private String message;
}
