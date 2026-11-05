package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品表 mall_product（MVP 均为虚拟电子商品）。
 */
@Data
public class MallProduct {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;

    /** 封面图URL（预留） */
    private String coverUrl;

    /** 关联文章ID（虚拟资料商品用；实物为空） */
    private Long articleId;

    /** 解锁所需积分 */
    private Integer price;

    /** 库存（虚拟商品同样占库存） */
    private Integer stock;

    /** 类型：1虚拟电子商品（预留2实物） */
    private Integer productType;

    /** 装扮类型：1头像框 2聊天气泡 3主页挂件；NULL=非装扮 */
    private Integer decorationKind;

    /**
     * 装扮资源：头像框/挂件为小程序包内透明 PNG 路径；
     * 气泡为前端 CSS 主题 key（lime/sakura/ocean/gold，小程序不支持 background-image 本地路径）
     */
    private String decorationAsset;

    private Integer status;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
