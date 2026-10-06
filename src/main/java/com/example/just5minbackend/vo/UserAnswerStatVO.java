package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 批量统计（按用户分组）——答题数/答对数。
 */
@Data
public class UserAnswerStatVO {

    private Long userId;
    private Long totalAnswered;
    private Long totalCorrect;
}
