package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.entity.Question;
import com.example.just5minbackend.mapper.FavoriteMapper;
import com.example.just5minbackend.mapper.QuestionMapper;
import com.example.just5minbackend.service.QuestionService;
import com.example.just5minbackend.support.QuestionVoConverter;
import com.example.just5minbackend.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final int DEFAULT_LIMIT = 10;
    /** 单次最多返回题数：顺序练习需要整卷拉取，分类题量上限按此约束 */
    private static final int MAX_LIMIT = 300;

    private final QuestionMapper questionMapper;
    private final FavoriteMapper favoriteMapper;
    private final QuestionVoConverter converter;

    @Override
    public List<QuestionVO> listQuestions(Long categoryId, Integer limit, String order) {
        if (categoryId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "categoryId 不能为空");
        }
        int size = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        List<Question> questions = "random".equalsIgnoreCase(order)
                ? questionMapper.listRandomByCategory(categoryId, size)
                : questionMapper.listByCategory(categoryId, size);

        List<QuestionVO> vos = questions.stream()
                .map(converter::toVO)
                .toList();
        fillFavorited(vos);
        return vos;
    }

    @Override
    public QuestionVO getQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null || question.getStatus() == null || question.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在或已下架");
        }
        QuestionVO vo = converter.toVO(question);
        fillFavorited(List.of(vo));
        return vo;
    }

    /**
     * 登录用户批量回填收藏标记；游客保持 null（表示未知，前端不显示收藏态）。
     */
    private void fillFavorited(List<QuestionVO> vos) {
        Long userId = UserContext.get();
        if (userId == null || vos.isEmpty()) {
            return;
        }
        List<Long> questionIds = vos.stream().map(QuestionVO::getId).filter(java.util.Objects::nonNull).toList();
        if (questionIds.isEmpty()) {
            return;
        }
        Set<Long> favoritedIds = new HashSet<>(favoriteMapper.listFavoritedIds(userId, questionIds));
        for (QuestionVO vo : vos) {
            vo.setFavorited(favoritedIds.contains(vo.getId()));
        }
    }
}
