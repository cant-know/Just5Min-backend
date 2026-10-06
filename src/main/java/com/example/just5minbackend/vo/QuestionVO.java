package com.example.just5minbackend.vo;

import lombok.Data;

import java.util.Map;

/**
 * 题目返回。answer / analysis 仅在需要时下发（刷题列表接口不下发，防作弊）。
 */
@Data
public class QuestionVO {

    private Long id;
    private Long categoryId;
    /** 题型：1单选 2多选 */
    private Integer questionType;
    private String content;
    /** 选项，如 {"A":"...","B":"..."} */
    private Map<String, String> options;
    private String answer;
    private String analysis;
    private Integer difficulty;
    /** 当前用户是否已收藏；游客（未登录）为 null */
    private Boolean favorited;
}
