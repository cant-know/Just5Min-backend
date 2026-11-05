package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商城分类表 mall_category（仅一级，不做多级）。
 */
@Data
public class MallCategory {

    private Long id;
    private String name;

    /** 业务类型：1资料文章（文章专栏复用） 2装扮商城 */
    private Integer bizType;

    /** 图标URL（预留） */
    private String icon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
