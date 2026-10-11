package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天页消息项。
 *
 * <p>{@code mine} 由 Service 层按「fromUserId 是否等于当前登录用户」算好，
 * 前端不必再自己比对，模板里直接靠它决定气泡左右与配色。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    /** 是否我发的（true=靠右显示，false=对方发来的靠左显示） */
    private Boolean mine;

    /** 对方是否已读（只在我发的消息上有意义；对方发来的恒为 null） */
    private Boolean read;

    private LocalDateTime createdAt;
}
