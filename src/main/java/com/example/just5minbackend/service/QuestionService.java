package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.QuestionVO;

import java.util.List;

public interface QuestionService {

    /**
     * 按分类拉取刷题列表（不含答案与解析）。
     *
     * @param order asc=按顺序，random=随机
     */
    List<QuestionVO> listQuestions(Long categoryId, Integer limit, String order);

    /**
     * 查询单题（用于错题重做等展示场景，不含答案与解析）。
     */
    QuestionVO getQuestion(Long id);
}
