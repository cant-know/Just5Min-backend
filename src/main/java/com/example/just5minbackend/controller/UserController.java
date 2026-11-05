package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.EquipDecorationDTO;
import com.example.just5minbackend.dto.UpdateProfileDTO;
import com.example.just5minbackend.service.DecorationService;
import com.example.just5minbackend.service.UserProfileService;
import com.example.just5minbackend.service.UserStatsService;
import com.example.just5minbackend.vo.DecorationVO;
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
 * 个人中心：学习统计 + 我的资料（编辑昵称/头像/资料字段）+ 装扮佩戴。
 * 全部接口需登录（/api/user/** 不在游客白名单里，未登录由 AuthInterceptor 直接 401）。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserStatsService userStatsService;
    private final UserProfileService userProfileService;
    private final DecorationService decorationService;

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

    /** 我的佩戴装扮（轻量：仅 3 组 id + 资源值，不含头像 Base64，聊天页每次进入都调） */
    @GetMapping("/decoration")
    public Result<DecorationVO> decoration() {
        return Result.success(decorationService.equipped(UserContext.require()));
    }

    /** 佩戴/摘下装扮（productId 传 null = 摘下） */
    @PutMapping("/decoration")
    public Result<Void> equip(@Valid @RequestBody EquipDecorationDTO dto) {
        decorationService.equip(UserContext.require(), dto.getKind(), dto.getProductId());
        return Result.success();
    }
}
