package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 分类搜索结果（只搜叶子分类，即没有子分类的分类）。
 */
@Data
public class CategorySearchVO {

    private Long id;
    private String name;
    /** 完整层级路径，如「学历提升/考研公共课/考研数学二」 */
    private String path;
    /** 该叶子分类下的上架题目数 */
    private Long questionCount;
}
