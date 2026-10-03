package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.AnswerRecord;
import com.example.just5minbackend.vo.CategoryStatVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnswerRecordMapper {

    int insert(AnswerRecord record);

    long countByUser(@Param("userId") Long userId);

    long countCorrectByUser(@Param("userId") Long userId);

    /**
     * 按分类统计用户的答题数与答对数。
     */
    List<CategoryStatVO> statsByCategory(@Param("userId") Long userId);
}
