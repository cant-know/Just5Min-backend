package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息表 message（好友之间一对一聊天）。
 *
 * <p>单行单方向：我发给好友存一行、好友发给我存一行。
 * 会话内容 = {@code (from=我 and to=好友) union (from=好友 and to=我)} 按 id 升序。</p>
 */
@Data
public class Message {

    private Long id;

    /** 发送者 */
    private Long fromUserId;

    /** 接收者 */
    private Long toUserId;

    /** 消息正文（纯文本，最长 500） */
    private String content;

    /** 是否已读：0未读 1已读（只有收件人能置已读） */
    private Integer isRead;

    private LocalDateTime createdAt;
}
