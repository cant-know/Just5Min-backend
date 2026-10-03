package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 单个分类的答题统计。
 */
@Data
public class CategoryStatVO {

    private Long categoryId;
    private String name;
    private Long answered;
    private Long correct;
}
