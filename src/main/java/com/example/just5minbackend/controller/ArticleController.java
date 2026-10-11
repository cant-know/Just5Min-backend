package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.service.ArticleService;
import com.example.just5minbackend.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学习资料专栏：游客可读（AuthInterceptor 已放行 GET /api/articles/**）。
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** 资料列表，categoryId 可选（游客可读），不下发正文 */
    @GetMapping
    public Result<List<ArticleVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.success(articleService.list(categoryId));
    }

    /** 资料详情（含正文，游客可读） */
    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.detail(id));
    }
}
