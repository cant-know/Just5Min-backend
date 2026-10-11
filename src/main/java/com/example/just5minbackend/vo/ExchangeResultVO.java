package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兑换（领取资料）成功结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResultVO {

    private Long recordId;
    private Long productId;
    private String productName;

    /** 资料对应的文章ID（前端据此直接跳文章详情；实物商品为 null） */
    private Long articleId;

    /** 消耗积分（免费资料为 0） */
    private Integer pointsCost;

    /** 兑换后积分余额 */
    private Integer pointsBalance;
}
