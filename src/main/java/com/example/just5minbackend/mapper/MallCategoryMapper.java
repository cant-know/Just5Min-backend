package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.MallCategory;

import java.util.List;

public interface MallCategoryMapper {

    /** 所有启用中的商城分类，按 sort,id 升序 */
    List<MallCategory> listAllEnabled();
}
