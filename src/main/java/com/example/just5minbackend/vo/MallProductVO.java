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

    /** 类型：1虚拟电子商品（预留2实物） */
    private Integer productType;

    /** 装扮类型：1头像框 2聊天气泡 3主页挂件；非装扮商品为 null */
    private Integer decorationKind;

    /** 装扮资源：头像框/挂件为透明 PNG 路径；气泡为 CSS 主题 key；非装扮为 null */
    private String decorationAsset;
}
