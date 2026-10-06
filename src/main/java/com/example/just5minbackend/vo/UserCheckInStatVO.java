package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 批量统计（按用户分组）——打卡天数。
 */
@Data
public class UserCheckInStatVO {

    private Long userId;
    private Long days;
}
