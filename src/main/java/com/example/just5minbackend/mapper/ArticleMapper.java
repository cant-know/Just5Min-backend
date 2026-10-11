package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleMapper {

    /** 已发布文章列表（不含 content），categoryId 可选过滤 */
    List<Article> listPublished(@Param("categoryId") Long categoryId);

    /** 文章详情（含 content），仅返回已发布的 */
    Article selectPublishedById(@Param("id") Long id);

    /** 阅读量 +1（非关键路径，不同步更新 updated_at 由 DB 自动处理） */
    int increaseReadCount(@Param("id") Long id);
}
