package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兑换成功结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResultVO {

    private Long recordId;
    private Long productId;
    private String productName;

    /** 消耗积分 */
    private Integer pointsCost;

    /** 兑换后积分余额 */
    private Integer pointsBalance;
}
