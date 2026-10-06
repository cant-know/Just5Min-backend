package com.example.just5minbackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收到的好友请求（收件箱项）。
 */
@Data
public class FriendRequestVO {

    private Long id;

    private Long fromUserId;

    private String nickname;

    /** 头像（Base64 DataURL），未设置为 null */
    private String avatarUrl;

    /** 验证附言 */
    private String message;

    private LocalDateTime createdAt;
}
