package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.SubmitAnswerDTO;
import com.example.just5minbackend.service.AnswerService;
import com.example.just5minbackend.vo.CheckResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 提交答案并判分；答错自动写入错题本。
     */
    @PostMapping("/submit")
    public Result<CheckResultVO> submit(@Valid @RequestBody SubmitAnswerDTO dto) {
        return Result.success(answerService.submit(UserContext.require(), dto));
    }
}
