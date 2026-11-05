package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.MallCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MallCategoryMapper {

    /** 所有启用中的资料分类，按 sort,id 升序（文章详情回填分类名用） */
    List<MallCategory> listAllEnabled();

    /** 按业务类型查启用分类：1资料文章 2装扮商城 */
    List<MallCategory> listEnabledByBizType(@Param("bizType") Integer bizType);

    /** 单个分类（文章详情回填分类名用） */
    MallCategory selectById(@Param("id") Long id);
}
