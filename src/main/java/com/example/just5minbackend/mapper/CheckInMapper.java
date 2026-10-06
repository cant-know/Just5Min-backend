package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.CheckIn;
import com.example.just5minbackend.vo.UserCheckInStatVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface CheckInMapper {

    /** 幂等插入今日打卡（uk_user_date 冲突时忽略），返回影响行数（0 = 今天已打过） */
    int insertIgnore(@Param("userId") Long userId, @Param("checkDate") LocalDate checkDate);

    /** 用户全部打卡日期（升序）；打卡记录量级为每天一条，全量拉取后在内存计算 */
    List<LocalDate> listDatesByUser(@Param("userId") Long userId);

    /** 某月打卡记录数（预留，当前未使用） */
    int countByUserBetween(@Param("userId") Long userId,
                           @Param("start") LocalDate start,
                           @Param("end") LocalDate end);

    /** 实体查询（预留） */
    List<CheckIn> listByUserBetween(@Param("userId") Long userId,
                                    @Param("start") LocalDate start,
                                    @Param("end") LocalDate end);

    /**
     * 批量统计一批用户的累计打卡天数（好友列表用）。
     * <p><b>注意</b>：{@code userIds} 不能为空集合，否则会生成非法的 {@code IN ()}。
     * 调用方需先判空短路。</p>
     */
    List<UserCheckInStatVO> countByUsers(@Param("userIds") Collection<Long> userIds);
}
