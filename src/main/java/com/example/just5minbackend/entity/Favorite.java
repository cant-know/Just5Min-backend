package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏表 favorite。
 */
@Data
public class Favorite {

    private Long id;
    private Long userId;
    private Long questionId;
    /** 冗余的分类ID，用于按分类筛选 */
    private Long categoryId;
    private LocalDateTime createdAt;
}
