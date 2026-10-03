package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.service.CategoryService;
import com.example.just5minbackend.vo.CategorySearchVO;
import com.example.just5minbackend.vo.CategoryTreeVO;
import com.example.just5minbackend.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 一级分类列表（旧接口，保留兼容）。
     */
    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.listCategories());
    }

    /**
     * 完整分类树，供「题库」页左右栏渲染。
     */
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.getTree());
    }

    /**
     * 叶子分类模糊搜索，返回带层级路径的结果。
     */
    @GetMapping("/search")
    public Result<List<CategorySearchVO>> search(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(categoryService.search(keyword));
    }
}
