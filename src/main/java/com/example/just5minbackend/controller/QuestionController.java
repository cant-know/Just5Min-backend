package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.service.QuestionService;
import com.example.just5minbackend.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 拉取刷题列表。
     *
     * @param order asc（默认，按顺序）或 random（随机）
     */
    @GetMapping
    public Result<List<QuestionVO>> list(@RequestParam Long categoryId,
                                         @RequestParam(required = false) Integer limit,
                                         @RequestParam(required = false, defaultValue = "asc") String order) {
        return Result.success(questionService.listQuestions(categoryId, limit, order));
    }

    @GetMapping("/{id}")
    public Result<QuestionVO> detail(@PathVariable Long id) {
        return Result.success(questionService.getQuestion(id));
    }
}
