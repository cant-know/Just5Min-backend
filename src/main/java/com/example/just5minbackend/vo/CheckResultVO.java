package com.example.just5minbackend.vo;

import lombok.Data;

/**
 * 提交答案后的判分结果。
 */
@Data
public class CheckResultVO {

    /** 是否答对 */
    private Boolean correct;

    /** 正确答案 */
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 本次获得积分（每次提交 +1） */
    private Integer pointsEarned;

    /** 结算后积分余额 */
    private Integer pointsTotal;
}
