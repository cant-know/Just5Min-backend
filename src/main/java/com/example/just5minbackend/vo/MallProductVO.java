package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 学习资料商品（列表/详情通用，不含成本类敏感字段）。
 */
@Data
public class MallProductVO {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String coverUrl;

    /** 关联文章ID（点进去读资料）；实物商品为 null */
    private Long articleId;

    /** 解锁所需积分（0 = 免费） */
    private Integer price;

    /** 剩余库存 */
    private Integer stock;

    /** 类型：1虚拟电子资料（预留2实物） */
    private Integer productType;
}
