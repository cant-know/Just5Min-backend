package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目表 question。
 * options 直接映射 JSON 列（JDBC 层以字符串读写），在 Service 层再转 Map。
 */
@Data
public class Question {

    private Long id;
    private Long categoryId;
    /** 题型：1单选 2多选（预留3判断4填空5简答） */
    private Integer questionType;
    private String content;
    /** 选项 JSON 字符串，如 {"A":"...","B":"..."} */
    private String options;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
