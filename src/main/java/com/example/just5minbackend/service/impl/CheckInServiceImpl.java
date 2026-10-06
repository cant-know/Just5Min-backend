package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.PointsLog;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.CheckInMapper;
import com.example.just5minbackend.mapper.PointsLogMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.CheckInService;
import com.example.just5minbackend.vo.CheckInResultVO;
import com.example.just5minbackend.vo.CheckInVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    /** 每日打卡奖励积分 */
    private static final int POINTS_PER_CHECK_IN = 10;
    /** 积分流水来源：1答题 2兑换 3打卡 */
    private static final int SOURCE_CHECK_IN = 3;

    private final CheckInMapper checkInMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    public CheckInVO summary(Long userId, YearMonth month) {
        List<LocalDate> allDates = checkInMapper.listDatesByUser(userId);
        Set<LocalDate> dateSet = new HashSet<>(allDates);

        YearMonth target = month == null ? YearMonth.now() : month;
        List<String> monthDates = allDates.stream()
                .filter(d -> YearMonth.from(d).equals(target))
                .map(LocalDate::toString)
                .toList();

        LocalDate today = LocalDate.now();
        return new CheckInVO(
                (long) allDates.size(),
                calcContinuous(dateSet, today),
                dateSet.contains(today),
                monthDates
        );
    }

    /**
     * 每日打卡：INSERT IGNORE 靠唯一键幂等，撞键即今日已打卡（1005）。
     * +10 积分与流水在同一事务内结算。
     */
    @Override
    @Transactional
    public CheckInResultVO checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        if (checkInMapper.insertIgnore(userId, today) == 0) {
            throw new BusinessException(ResultCode.ALREADY_CHECKED_IN);
        }

        userMapper.addPoints(userId, POINTS_PER_CHECK_IN);

        int balance = readPoints(userId);
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(POINTS_PER_CHECK_IN);
        log.setBalance(balance);
        log.setSource(SOURCE_CHECK_IN);
        log.setRemark("每日打卡");
        pointsLogMapper.insert(log);

        Set<LocalDate> dateSet = new HashSet<>(checkInMapper.listDatesByUser(userId));
        return new CheckInResultVO(
                (long) dateSet.size(),
                calcContinuous(dateSet, today),
                POINTS_PER_CHECK_IN,
                balance
        );
    }

    /**
     * 连续打卡天数：从今天往回数（今天没打卡时从昨天起算，避免"今天还没打卡"把连击清零）；
     * 最近一次打卡早于昨天则连击已断。
     */
    private int calcContinuous(Set<LocalDate> dateSet, LocalDate today) {
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
        int continuous = 0;
        while (dateSet.contains(cursor)) {
            continuous++;
            cursor = cursor.minusDays(1);
        }
        return continuous;
    }

    private int readPoints(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null || user.getPoints() == null ? 0 : user.getPoints();
    }
}
