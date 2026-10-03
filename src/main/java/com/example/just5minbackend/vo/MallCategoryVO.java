package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商城分类（仅一级）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MallCategoryVO {

    private Long id;
    private String name;
}
