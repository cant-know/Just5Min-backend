package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 分类返回（一级分类 + 题目数量）。
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Long questionCount;
}
