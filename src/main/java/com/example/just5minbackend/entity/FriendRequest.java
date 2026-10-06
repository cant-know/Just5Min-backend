package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友请求表 friend_request。
 * <p>status: 0待处理 1已同意 2已拒绝。uk_from_to 保证同方向只有一行，
 * 被拒绝后再次申请走 upsert 把 status 复位为 0。</p>
 */
@Data
public class FriendRequest {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Integer status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
