package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.entity.Category;
import com.example.just5minbackend.mapper.CategoryMapper;
import com.example.just5minbackend.service.CategoryService;
import com.example.just5minbackend.vo.CategoryCountVO;
import com.example.just5minbackend.vo.CategorySearchVO;
import com.example.just5minbackend.vo.CategoryTreeVO;
import com.example.just5minbackend.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final long ROOT_PARENT_ID = 0L;

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> listCategories() {
        return categoryMapper.listTopLevelWithCount();
    }

    @Override
    public List<CategoryTreeVO> getTree() {
        List<Category> all = categoryMapper.listAllEnabled();
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> countMap = new HashMap<>();
        for (CategoryCountVO c : categoryMapper.countQuestionsByCategory()) {
            countMap.put(c.getCategoryId(), c.getQuestionCount());
        }

        // 先把所有分类建成节点（LinkedHashMap 保留 SQL 的 parent_id, sort 顺序）
        Map<Long, CategoryTreeVO> nodeMap = new LinkedHashMap<>();
        for (Category c : all) {
            CategoryTreeVO node = new CategoryTreeVO();
            node.setId(c.getId());
            node.setName(c.getName());
            node.setQuestionCount(countMap.getOrDefault(c.getId(), 0L));
            node.setChildren(new ArrayList<>());
            nodeMap.put(c.getId(), node);
        }

        // 再挂父子关系
        List<CategoryTreeVO> roots = new ArrayList<>();
        for (Category c : all) {
            CategoryTreeVO node = nodeMap.get(c.getId());
            Long parentId = c.getParentId();
            if (parentId == null || parentId == ROOT_PARENT_ID) {
                roots.add(node);
                continue;
            }
            CategoryTreeVO parent = nodeMap.get(parentId);
            if (parent == null) {
                // 父分类被停用/不存在时，按一级处理，避免节点丢失
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        // 后序遍历：把子孙题数累加到父节点
        for (CategoryTreeVO root : roots) {
            accumulate(root);
        }
        return roots;
    }

    @Override
    public List<CategorySearchVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<CategorySearchVO> hits = categoryMapper.searchLeaves(keyword.trim());
        if (hits.isEmpty()) {
            return hits;
        }

        // 用全量分类构建 id -> name / id -> parentId，供拼路径使用
        List<Category> all = categoryMapper.listAllEnabled();
        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, Long> parentMap = new HashMap<>();
        for (Category c : all) {
            nameMap.put(c.getId(), c.getName());
            parentMap.put(c.getId(), c.getParentId() == null ? ROOT_PARENT_ID : c.getParentId());
        }

        for (CategorySearchVO hit : hits) {
            hit.setPath(buildPath(hit.getId(), nameMap, parentMap));
        }
        return hits;
    }

    /**
     * 沿 parentId 上溯拼出完整路径，如「学历提升/考研公共课/考研数学二」。
     */
    private String buildPath(Long id, Map<Long, String> nameMap, Map<Long, Long> parentMap) {
        LinkedList<String> parts = new LinkedList<>();
        Long cursor = id;
        // 防御性上限：层级再深也不会超过 10 层，避免脏数据成环时死循环
        int guard = 0;
        while (cursor != null && cursor != ROOT_PARENT_ID && guard++ < 10) {
            String name = nameMap.get(cursor);
            if (name == null) {
                break;
            }
            parts.addFirst(name);
            cursor = parentMap.get(cursor);
        }
        return String.join("/", parts);
    }

    /**
     * 递归累加子孙题数，返回该节点累计后的题数。
     */
    private long accumulate(CategoryTreeVO node) {
        long total = node.getQuestionCount() == null ? 0L : node.getQuestionCount();
        List<CategoryTreeVO> children = node.getChildren();
        if (children != null) {
            for (CategoryTreeVO child : children) {
                total += accumulate(child);
            }
        }
        node.setQuestionCount(total);
        return total;
    }
}
