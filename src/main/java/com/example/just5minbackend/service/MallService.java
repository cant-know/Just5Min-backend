package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.ExchangeRecordVO;
import com.example.just5minbackend.vo.ExchangeResultVO;
import com.example.just5minbackend.vo.MallCategoryVO;
import com.example.just5minbackend.vo.MallProductDetailVO;
import com.example.just5minbackend.vo.MallProductVO;

import java.util.List;

/**
 * 积分商城（虚拟装扮）：分类/商品浏览（游客可读），兑换与兑换记录（需登录）。
 */
public interface MallService {

    /**
     * 分类列表。
     *
     * @param bizType 1资料文章（文章专栏复用） 2装扮商城；null 时按 1 处理
     */
    List<MallCategoryVO> listCategories(Integer bizType);

    List<MallProductVO> listProducts(Long categoryId);

    /**
     * 商品详情。
     *
     * @param userId 当前登录用户ID；游客传 null（此时 owned/equipped 为 null）
     */
    MallProductDetailVO getProduct(Long productId, Long userId);

    ExchangeResultVO exchange(Long userId, Long productId);

    List<ExchangeRecordVO> listExchanges(Long userId);
}
