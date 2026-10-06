package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.UpdateProfileDTO;
import com.example.just5minbackend.service.UserProfileService;
import com.example.just5minbackend.service.UserStatsService;
import com.example.just5minbackend.vo.UserProfileVO;
import com.example.just5minbackend.vo.UserStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心：学习统计 + 我的资料（编辑昵称/头像）。
 * 全部接口需登录（/api/user/** 不在游客白名单里，未登录由 AuthInterceptor 直接 401）。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserStatsService userStatsService;
    private final UserProfileService userProfileService;

    @GetMapping("/stats")
    public Result<UserStatsVO> stats() {
        return Result.success(userStatsService.stats(UserContext.require()));
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.success(userProfileService.get(UserContext.require()));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return Result.success(userProfileService.update(UserContext.require(), dto));
    }
}
