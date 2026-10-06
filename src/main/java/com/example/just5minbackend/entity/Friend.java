package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友关系表 friend（双向各存一行：A-B 成为好友时写 (A,B) 与 (B,A)）。
 */
@Data
public class Friend {

    private Long id;

    /** 关系拥有者 */
    private Long userId;

    /** 对方用户ID */
    private Long friendId;

    private LocalDateTime createdAt;
}
