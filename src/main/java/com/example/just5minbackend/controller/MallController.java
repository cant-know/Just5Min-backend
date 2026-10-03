package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.ExchangeProductDTO;
import com.example.just5minbackend.service.MallService;
import com.example.just5minbackend.vo.ExchangeRecordVO;
import com.example.just5minbackend.vo.ExchangeResultVO;
import com.example.just5minbackend.vo.MallCategoryVO;
import com.example.just5minbackend.vo.MallProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 积分商城：分类/商品游客可读，兑换与兑换记录需登录。
 */
@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    /** 商城一级分类（游客可读） */
    @GetMapping("/categories")
    public Result<List<MallCategoryVO>> categories() {
        return Result.success(mallService.listCategories());
    }

    /** 商品列表，categoryId 可选（游客可读） */
    @GetMapping("/products")
    public Result<List<MallProductVO>> products(
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(mallService.listProducts(categoryId));
    }

    /** 兑换商品（需登录） */
    @PostMapping("/exchange")
    public Result<ExchangeResultVO> exchange(@Valid @RequestBody ExchangeProductDTO dto) {
        return Result.success(mallService.exchange(UserContext.require(), dto.getProductId()));
    }

    /** 我的兑换记录（需登录） */
    @GetMapping("/exchanges")
    public Result<List<ExchangeRecordVO>> exchanges() {
        return Result.success(mallService.listExchanges(UserContext.require()));
    }
}
