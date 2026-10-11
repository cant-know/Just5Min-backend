package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.Article;
import com.example.just5minbackend.entity.MallCategory;
import com.example.just5minbackend.mapper.ArticleMapper;
import com.example.just5minbackend.mapper.MallCategoryMapper;
import com.example.just5minbackend.service.ArticleService;
import com.example.just5minbackend.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final MallCategoryMapper mallCategoryMapper;

    @Override
    public List<ArticleVO> list(Long categoryId) {
        List<Article> articles = articleMapper.listPublished(categoryId);
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> categoryNames = new HashMap<>();
        for (MallCategory c : mallCategoryMapper.listAllEnabled()) {
            categoryNames.put(c.getId(), c.getName());
        }

        return articles.stream()
                .map(a -> toVO(a, categoryNames.get(a.getCategoryId()), false))
                .toList();
    }

    @Override
    public ArticleVO detail(Long id) {
        Article article = articleMapper.selectPublishedById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        // 阅读量自增失败不影响阅读体验，故不校验影响行数
        articleMapper.increaseReadCount(id);
        article.setReadCount(article.getReadCount() == null ? 1 : article.getReadCount() + 1);

        MallCategory category = mallCategoryMapper.selectById(article.getCategoryId());
        return toVO(article, category == null ? null : category.getName(), true);
    }

    /**
     * @param withContent 列表场景传 false，避免下发 MEDIUMTEXT 正文
     */
    private ArticleVO toVO(Article a, String categoryName, boolean withContent) {
        ArticleVO vo = new ArticleVO();
        vo.setId(a.getId());
        vo.setCategoryId(a.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setTitle(a.getTitle());
        vo.setSummary(a.getSummary());
        vo.setTags(a.getTags());
        vo.setTagList(splitTags(a.getTags()));
        vo.setCoverUrl(a.getCoverUrl());
        vo.setReadCount(a.getReadCount());
        vo.setCreatedAt(a.getCreatedAt());
        if (withContent) {
            vo.setContent(a.getContent());
        }
        return vo;
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
