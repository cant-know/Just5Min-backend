package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.service.CheckInService;
import com.example.just5minbackend.vo.CheckInResultVO;
import com.example.just5minbackend.vo.CheckInVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    /**
     * 打卡概览（需登录）。
     *
     * @param month 可选，格式 yyyy-MM；不传默认当月（返回该月已打卡日期供月历标记）
     */
    @GetMapping
    public Result<CheckInVO> summary(@RequestParam(required = false) String month) {
        Long userId = UserContext.require();
        return Result.success(checkInService.summary(userId, parseMonth(month)));
    }

    /** 每日打卡（需登录）：首次 +10 积分，重复打卡返回 1005 */
    @PostMapping
    public Result<CheckInResultVO> checkIn() {
        Long userId = UserContext.require();
        return Result.success(checkInService.checkIn(userId));
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "month 格式应为 yyyy-MM");
        }
    }
}
