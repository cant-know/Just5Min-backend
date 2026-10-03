package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水表 points_log。
 * change_amount 正为获取、负为消耗；balance 为变动后余额快照。
 */
@Data
public class PointsLog {

    private Long id;
    private Long userId;

    /** 变动值，正获取负消耗 */
    private Integer changeAmount;

    /** 变动后余额快照 */
    private Integer balance;

    /** 来源：1答题 2兑换 */
    private Integer source;

    /** 关联业务ID（答题记录ID/兑换记录ID） */
    private Long refId;
    private String remark;
    private LocalDateTime createdAt;
}
