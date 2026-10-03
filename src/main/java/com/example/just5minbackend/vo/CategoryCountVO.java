package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 分类题目数统计（GROUP BY category_id 的查询结果载体）。
 */
@Data
public class CategoryCountVO {

    private Long categoryId;
    private Long questionCount;
}
