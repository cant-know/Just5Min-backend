package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.service.UserStatsService;
import com.example.just5minbackend.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserStatsService userStatsService;

    @GetMapping("/stats")
    public Result<UserStatsVO> stats() {
        return Result.success(userStatsService.stats(UserContext.require()));
    }
}
