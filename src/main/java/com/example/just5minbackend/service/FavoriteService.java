package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.FavoriteVO;

import java.util.List;

public interface FavoriteService {

    List<FavoriteVO> list(Long userId, Long categoryId);

    /** 收藏题目（幂等，重复收藏不报错）。 */
    void add(Long userId, Long questionId);

    void remove(Long userId, Long questionId);
}
