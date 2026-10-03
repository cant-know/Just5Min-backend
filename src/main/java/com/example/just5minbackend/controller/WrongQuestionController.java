package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.service.WrongQuestionService;
import com.example.just5minbackend.vo.WrongQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping
    public Result<List<WrongQuestionVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.success(wrongQuestionService.list(UserContext.require(), categoryId));
    }

    @DeleteMapping("/{questionId}")
    public Result<Boolean> remove(@PathVariable Long questionId) {
        wrongQuestionService.remove(UserContext.require(), questionId);
        return Result.success(true);
    }
}
