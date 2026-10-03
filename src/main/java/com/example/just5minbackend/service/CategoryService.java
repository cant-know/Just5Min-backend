package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.CategorySearchVO;
import com.example.just5minbackend.vo.CategoryTreeVO;
import com.example.just5minbackend.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /**
     * 一级分类 + 题目数（旧接口，保留兼容）。
     */
    List<CategoryVO> listCategories();

    /**
     * 完整分类树（一级 → 二级分组/直挂叶子 → 三级叶子）。
     * 每个节点的 questionCount 为含子孙的累计题数。
     */
    List<CategoryTreeVO> getTree();

    /**
     * 按关键字模糊搜索叶子分类，返回带层级路径的结果。
     * 关键字为空时返回空列表。
     */
    List<CategorySearchVO> search(String keyword);
}
