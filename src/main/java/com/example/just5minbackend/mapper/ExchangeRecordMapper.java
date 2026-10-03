package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.ExchangeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExchangeRecordMapper {

    int insert(ExchangeRecord record);

    /** 某用户的兑换记录，按兑换时间倒序 */
    List<ExchangeRecord> listByUser(@Param("userId") Long userId);
}
