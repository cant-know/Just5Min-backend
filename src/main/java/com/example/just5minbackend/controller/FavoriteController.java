package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.service.FavoriteService;
import com.example.just5minbackend.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 题目收藏：全部接口需登录（未登录由 AuthInterceptor 直接 401）。
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Result<List<FavoriteVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.success(favoriteService.list(UserContext.require(), categoryId));
    }

    @PostMapping("/{questionId}")
    public Result<Boolean> add(@PathVariable Long questionId) {
        favoriteService.add(UserContext.require(), questionId);
        return Result.success(true);
    }

    @DeleteMapping("/{questionId}")
    public Result<Boolean> remove(@PathVariable Long questionId) {
        favoriteService.remove(UserContext.require(), questionId);
        return Result.success(true);
    }
}
