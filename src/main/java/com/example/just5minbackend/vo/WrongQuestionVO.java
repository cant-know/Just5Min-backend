package com.example.just5minbackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本条目返回。
 */
@Data
public class WrongQuestionVO {

    private Integer wrongCount;
    private String lastWrongAnswer;
    private LocalDateTime lastWrongAt;
    private QuestionVO question;
}
