package com.example.just5minbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本表 wrong_question。
 */
@Data
public class WrongQuestion {

    private Long id;
    private Long userId;
    private Long questionId;
    private Long categoryId;
    private Integer wrongCount;
    private String lastWrongAnswer;
    /** 掌握状态：0未掌握 1已掌握（预留） */
    private Integer masterStatus;
    private LocalDateTime lastWrongAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
