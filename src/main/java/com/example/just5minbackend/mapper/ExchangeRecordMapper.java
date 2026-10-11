package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.ExchangeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExchangeRecordMapper {

    int insert(ExchangeRecord record);

    /** 某用户的兑换记录，按兑换时间倒序 */
    List<ExchangeRecord> listByUser(@Param("userId") Long userId);

    /** 查某用户对某商品的最近一条领取记录（免费资料幂等用） */
    ExchangeRecord selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
