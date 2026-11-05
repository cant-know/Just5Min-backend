package com.example.just5minbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 佩戴 / 摘下装扮。
 * kind：1头像框 2聊天气泡 3主页挂件；
 * productId 传 null 表示摘下该槽位。
 */
@Data
public class EquipDecorationDTO {

    @NotNull(message = "缺少装扮类型")
    @Min(value = 1, message = "装扮类型不合法")
    @Max(value = 3, message = "装扮类型不合法")
    private Integer kind;

    /** null = 摘下 */
    private Long productId;
}
