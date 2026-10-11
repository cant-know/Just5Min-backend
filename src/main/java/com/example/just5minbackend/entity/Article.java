package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资料文章表 article。
 * content 为长文正文（MEDIUMTEXT），列表接口不下发该字段。
 */
@Data
public class Article {

    private Long id;
    private Long categoryId;
    private String title;
    private String summary;
    private String content;
    private String tags;
    private String coverUrl;
    private Integer readCount;
    private Integer status;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
