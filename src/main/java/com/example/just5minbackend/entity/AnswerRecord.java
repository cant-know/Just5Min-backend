package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答题记录表 answer_record。
 */
@Data
public class AnswerRecord {

    private Long id;
    private Long userId;
    private Long questionId;
    /** 冗余分类 ID，便于按分类统计 */
    private Long categoryId;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer durationMs;
    private LocalDateTime createdAt;
}
