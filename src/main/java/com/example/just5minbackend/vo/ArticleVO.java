package com.example.just5minbackend.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习资料文章返回。
 * <p>列表接口下发的对象 content 为 null；详情接口才带正文。</p>
 */
@Data
public class ArticleVO {

    private Long id;
    private Long categoryId;
    /** 分类名称（列表页副标题用） */
    private String categoryName;
    private String title;
    private String summary;
    /** 正文，仅详情接口下发 */
    private String content;
    /** 标签（原始逗号分隔串） */
    private String tags;
    /** 标签数组，前端直接 v-for */
    private List<String> tagList;
    private String coverUrl;
    private Integer readCount;
    private LocalDateTime createdAt;
}
