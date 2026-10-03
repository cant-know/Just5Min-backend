package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.WrongQuestionVO;

import java.util.List;

public interface WrongQuestionService {

    List<WrongQuestionVO> list(Long userId, Long categoryId);

    void remove(Long userId, Long questionId);
}
