package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 打卡概览（打卡页首屏）。
 * dates 为所查询月份的已打卡日期（yyyy-MM-dd），用于月历标记。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInVO {

    /** 累计打卡天数 */
    private Long totalDays;

    /** 连续打卡天数（今天未打卡时按到昨天计算，显示不断电） */
    private Integer continuousDays;

    /** 今日是否已打卡 */
    private Boolean checkedToday;

    /** 所查月份的已打卡日期列表（yyyy-MM-dd） */
    private List<String> dates;
}
