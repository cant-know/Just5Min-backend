package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友列表项（含对方学习数据，用于「好友」页列表展示）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO {

    private Long userId;

    private String nickname;

    /** 头像（Base64 DataURL），未设置为 null */
    private String avatarUrl;

    /** 累计答题数 */
    private Long totalAnswered;

    /** 正确率（0~1，保留两位小数） */
    private Double accuracy;

    /** 累计打卡天数 */
    private Long checkinDays;

    /** 积分余额 */
    private Integer points;

    /** 成为好友时间 */
    private LocalDateTime friendSince;
}
