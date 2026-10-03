package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表 category。
 * parentId = 0 表示一级分类，二级分类为后续扩展预留。
 */
@Data
public class Category {

    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
