package com.example.just5minbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交答案入参。
 */
@Data
public class SubmitAnswerDTO {

    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @NotBlank(message = "userAnswer 不能为空")
    private String userAnswer;

    /** 本题用时毫秒（可选） */
    private Integer durationMs;
}
