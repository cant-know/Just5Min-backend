package com.example.just5minbackend.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点（题库页左右栏渲染用）。
 * questionCount 为该分类**及其所有子孙分类**的上架题目总数：
 * 叶子分类为自身题数，分组/一级分类为累计值。
 * children 为空时返回空数组（非 null），前端可直接 v-for。
 */
@Data
public class CategoryTreeVO {

    private Long id;
    private String name;
    /** 含子孙分类的题目总数 */
    private Long questionCount;
    /** 子分类；叶子分类为空数组 */
    private List<CategoryTreeVO> children = new ArrayList<>();
}
