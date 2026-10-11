package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.ArticleVO;

import java.util.List;

/**
 * 学习资料专栏：游客可读。
 */
public interface ArticleService {

    /** 资料列表（不含正文），categoryId 可选 */
    List<ArticleVO> list(Long categoryId);

    /** 资料详情（含正文），同时累加阅读量 */
    ArticleVO detail(Long id);
}
