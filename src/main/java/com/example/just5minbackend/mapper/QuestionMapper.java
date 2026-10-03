package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionMapper {

    /**
     * 按分类顺序取题（不含 answer / analysis，防作弊）。
     */
    List<Question> listByCategory(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    /**
     * 按分类随机取题（不含 answer / analysis）。
     */
    List<Question> listRandomByCategory(@Param("categoryId") Long categoryId, @Param("limit") int limit);

    /**
     * 按 ID 查完整题目（含 answer / analysis，供判分使用）。
     */
    Question selectById(@Param("id") Long id);

    /**
     * 批量按 ID 查题（不含 answer / analysis）。
     */
    List<Question> listByIds(@Param("ids") List<Long> ids);
}
