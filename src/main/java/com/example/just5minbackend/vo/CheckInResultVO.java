package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打卡结果（POST /api/checkins 成功后返回）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResultVO {

    /** 累计打卡天数（含本次） */
    private Long totalDays;

    /** 连续打卡天数（含本次） */
    private Integer continuousDays;

    /** 本次获得积分 */
    private Integer pointsEarned;

    /** 打卡后积分余额 */
    private Integer pointsBalance;
}
