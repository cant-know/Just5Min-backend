package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录表 exchange_record。
 * product_name / points_cost 为兑换时刻快照，商品后续改名/调价不影响历史。
 */
@Data
public class ExchangeRecord {

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;

    /** 消耗积分快照 */
    private Integer pointsCost;

    /** 状态：1已兑换（预留2已发放 3已取消） */
    private Integer status;
    private LocalDateTime createdAt;
}
