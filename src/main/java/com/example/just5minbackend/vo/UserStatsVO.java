package com.example.just5minbackend.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户学习统计。
 */
@Data
public class UserStatsVO {

    /** 总答题数 */
    private Long totalAnswered;

    /** 总答对题数 */
    private Long totalCorrect;

    /** 正确率（0~1，保留两位小数） */
    private Double accuracy;

    /** 错题数量 */
    private Long wrongCount;

    /** 各分类答题分布 */
    private List<CategoryStatVO> perCategory;

    /** 积分余额 */
    private Integer points;
}
