package com.example.just5minbackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录（我的-兑换记录页）。
 */
@Data
public class ExchangeRecordVO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer pointsCost;
    private Integer status;
    private LocalDateTime createdAt;
}
