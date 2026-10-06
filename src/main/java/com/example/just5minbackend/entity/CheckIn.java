package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日打卡记录表 check_in。
 * 每用户每天最多一条，靠 uk_user_date 唯一键保证。
 */
@Data
public class CheckIn {

    private Long id;
    private Long userId;

    /** 打卡日期（自然日） */
    private LocalDate checkDate;

    private LocalDateTime createdAt;
}
