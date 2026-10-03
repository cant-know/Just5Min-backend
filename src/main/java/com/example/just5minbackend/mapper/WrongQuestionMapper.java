package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.WrongQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WrongQuestionMapper {

    /**
     * 答错时写入错题本：不存在则插入，存在则错误次数 +1 并刷新最近答错信息。
     */
    int upsert(WrongQuestion wrongQuestion);

    List<WrongQuestion> listByUser(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    int deleteByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    long countByUser(@Param("userId") Long userId);
}
