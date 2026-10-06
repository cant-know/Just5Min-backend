package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper {

    /**
     * 收藏题目：同一用户同一题只保留一条（INSERT IGNORE 幂等）。
     */
    int insertIgnore(Favorite favorite);

    List<Favorite> listByUser(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    int deleteByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    /** 某分类下已收藏的题目 ID 集合（用于题目列表回填 favorited 标记）。 */
    List<Long> listQuestionIds(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /** 批量判断这些题目里哪些已被该用户收藏。 */
    List<Long> listFavoritedIds(@Param("userId") Long userId, @Param("questionIds") List<Long> questionIds);

    long countByUser(@Param("userId") Long userId);
}
