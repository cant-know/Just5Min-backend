package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.ExchangeProductDTO;
import com.example.just5minbackend.service.MallService;
import com.example.just5minbackend.vo.ExchangeRecordVO;
import com.example.just5minbackend.vo.ExchangeResultVO;
import com.example.just5minbackend.vo.MallCategoryVO;
import com.example.just5minbackend.vo.MallProductDetailVO;
import com.example.just5minbackend.vo.MallProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 积分商城（虚拟装扮）：分类/商品/详情游客可读，兑换与兑换记录需登录。
 */
@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    /**
     * 商城分类（游客可读）。
     *
     * @param bizType 1资料文章（文章专栏用，默认） 2装扮商城
     */
    @GetMapping("/categories")
    public Result<List<MallCategoryVO>> categories(
            @RequestParam(value = "bizType", defaultValue = "1") Integer bizType) {
        return Result.success(mallService.listCategories(bizType));
    }

    /** 商品列表，categoryId 可选（游客可读） */
    @GetMapping("/products")
    public Result<List<MallProductVO>> products(
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(mallService.listProducts(categoryId));
    }

    /**
     * 商品详情（游客可读）。
     * 登录时额外返回 owned / equipped；游客时两者为 null。
     */
    @GetMapping("/products/{id}")
    public Result<MallProductDetailVO> product(@PathVariable("id") Long id) {
        return Result.success(mallService.getProduct(id, UserContext.get()));
    }

    /** 兑换商品（需登录）：免费商品不扣积分且幂等；付费商品扣库存扣积分；装扮自动佩戴 */
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
