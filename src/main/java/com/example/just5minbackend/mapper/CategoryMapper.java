package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.Category;
import com.example.just5minbackend.vo.CategoryCountVO;
import com.example.just5minbackend.vo.CategorySearchVO;
import com.example.just5minbackend.vo.CategoryVO;

import java.util.List;

public interface CategoryMapper {

    /**
     * 查询所有启用的一级分类（parent_id = 0），并附带该分类下上架题目数量。
     */
    List<CategoryVO> listTopLevelWithCount();

    /**
     * 查询全部启用分类（含一/二/三级），用于内存组树。
     * 排序：parent_id, sort, id —— 保证同层顺序即最终展示顺序。
     */
    List<Category> listAllEnabled();

    /**
     * 按分类统计上架题目数（GROUP BY category_id）。
     * 未出现在结果里的分类题数为 0。
     */
    List<CategoryCountVO> countQuestionsByCategory();

    /**
     * 模糊搜索**叶子分类**（没有启用子分类的分类），附叶子自身题数。
     * keyword 由调用方 trim，SQL 内使用 #{} 绑定防注入。
     */
    List<CategorySearchVO> searchLeaves(String keyword);
}
