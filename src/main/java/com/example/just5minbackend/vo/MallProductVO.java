package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 商城商品（列表/详情通用，不含成本类敏感字段）。
 */
@Data
public class MallProductVO {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String coverUrl;

    /** 兑换所需积分 */
    private Integer price;

    /** 剩余库存 */
    private Integer stock;

    /** 类型：1虚拟电子商品（预留2实物） */
    private Integer productType;
}
