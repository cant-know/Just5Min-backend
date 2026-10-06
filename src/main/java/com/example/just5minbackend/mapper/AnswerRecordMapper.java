package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.AnswerRecord;
import com.example.just5minbackend.vo.CategoryStatVO;
import com.example.just5minbackend.vo.UserAnswerStatVO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface AnswerRecordMapper {

    int insert(AnswerRecord record);

    long countByUser(@Param("userId") Long userId);

    long countCorrectByUser(@Param("userId") Long userId);

    /**
     * 按分类统计用户的答题数与答对数。
     */
    List<CategoryStatVO> statsByCategory(@Param("userId") Long userId);

    /**
     * 批量统计一批用户的答题数/答对数（好友列表用）。
     * <p><b>注意</b>：{@code userIds} 不能为空集合，否则会生成非法的 {@code IN ()}。
     * 调用方需先判空短路。</p>
     */
    List<UserAnswerStatVO> statsByUsers(@Param("userIds") Collection<Long> userIds);
}
