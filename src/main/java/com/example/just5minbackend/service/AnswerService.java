package com.example.just5minbackend.service;

import com.example.just5minbackend.dto.SubmitAnswerDTO;
import com.example.just5minbackend.vo.CheckResultVO;

public interface AnswerService {

    /**
     * 提交答案：规范化 → 判分 → 写答题记录 → 答错则写入错题本。
     */
    CheckResultVO submit(Long userId, SubmitAnswerDTO dto);
}
