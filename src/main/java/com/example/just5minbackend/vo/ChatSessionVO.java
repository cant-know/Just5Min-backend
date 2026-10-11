package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表项（每个好友一条，取最近一次互动的对方）。
 *
 * <p>用于「好友」页展示「最后一条消息 + 未读红点」，
 * 长按进聊天页时不必再拉一次历史。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {

    /** 对方用户ID */
    private Long userId;

    private String nickname;

    /** 头像（Base64 DataURL），未设置为 null */
    private String avatarUrl;

    /** 最后一条消息正文 */
    private String lastContent;

    /** 最后一条消息是否我发的 */
    private Boolean lastMine;

    private LocalDateTime lastTime;

    /** 对方发给我的未读条数 */
    private Long unreadCount;
}
