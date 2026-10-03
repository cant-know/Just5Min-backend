package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.ExchangeRecordVO;
import com.example.just5minbackend.vo.ExchangeResultVO;
import com.example.just5minbackend.vo.MallCategoryVO;
import com.example.just5minbackend.vo.MallProductVO;

import java.util.List;

/**
 * 积分商城：分类 / 商品浏览（游客可读），兑换与兑换记录（需登录）。
 */
public interface MallService {

    List<MallCategoryVO> listCategories();

    List<MallProductVO> listProducts(Long categoryId);

    ExchangeResultVO exchange(Long userId, Long productId);

    List<ExchangeRecordVO> listExchanges(Long userId);
}
