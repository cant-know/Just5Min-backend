package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.CheckInResultVO;
import com.example.just5minbackend.vo.CheckInVO;

import java.time.YearMonth;

public interface CheckInService {

    /** 打卡概览：累计/连续天数、今日是否已打卡、指定月份的打卡日期 */
    CheckInVO summary(Long userId, YearMonth month);

    /** 每日打卡：首次 +10 积分，重复打卡抛 1005 */
    CheckInResultVO checkIn(Long userId);
}
