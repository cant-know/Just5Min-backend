package com.example.just5minbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 兑换商品入参。
 */
@Data
public class ExchangeProductDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
