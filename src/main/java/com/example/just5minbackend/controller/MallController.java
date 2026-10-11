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
 * 学习资料（原积分商城）：分类/资料列表游客可读，领取与领取记录需登录。
 */
@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    /** 资料分类（游客可读） */
    @GetMapping("/categories")
    public Result<List<MallCategoryVO>> categories() {
        return Result.success(mallService.listCategories());
    }

    /** 资料列表，categoryId 可选（游客可读） */
    @GetMapping("/products")
    public Result<List<MallProductVO>> products(
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(mallService.listProducts(categoryId));
    }

    /** 领取资料（需登录）：免费资料不扣积分且幂等；付费商品扣库存扣积分 */
    @PostMapping("/exchange")
    public Result<ExchangeResultVO> exchange(@Valid @RequestBody ExchangeProductDTO dto) {
        return Result.success(mallService.exchange(UserContext.require(), dto.getProductId()));
    }

    /** 我的领取记录（需登录） */
    @GetMapping("/exchanges")
    public Result<List<ExchangeRecordVO>> exchanges() {
        return Result.success(mallService.listExchanges(UserContext.require()));
    }
}
