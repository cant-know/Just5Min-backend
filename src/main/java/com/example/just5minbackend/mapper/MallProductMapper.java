package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.MallProduct;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface MallProductMapper {

    /** 上架商品列表，categoryId 可选过滤，按 category_id,sort,id 升序 */
    List<MallProduct> listEnabled(@Param("categoryId") Long categoryId);

    MallProduct selectById(@Param("id") Long id);

    /** 按 id 批量查（好友列表批量下发装扮用；调用方须保证 ids 非空） */
    List<MallProduct> listByIds(@Param("ids") Collection<Long> ids);

    /** 条件扣库存：stock > 0 才扣，返回影响行数（0 = 库存不足） */
    int deductStock(@Param("id") Long id);
}
