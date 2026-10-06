package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.Favorite;
import com.example.just5minbackend.entity.Question;
import com.example.just5minbackend.mapper.FavoriteMapper;
import com.example.just5minbackend.mapper.QuestionMapper;
import com.example.just5minbackend.service.FavoriteService;
import com.example.just5minbackend.support.QuestionVoConverter;
import com.example.just5minbackend.vo.FavoriteVO;
import com.example.just5minbackend.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final QuestionMapper questionMapper;
    private final QuestionVoConverter converter;

    @Override
    public List<FavoriteVO> list(Long userId, Long categoryId) {
        List<Favorite> favorites = favoriteMapper.listByUser(userId, categoryId);
        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = favorites.stream().map(Favorite::getQuestionId).toList();
        Map<Long, Question> questionMap = questionMapper.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));

        return favorites.stream()
                .filter(f -> questionMap.containsKey(f.getQuestionId()))
                .map(f -> {
                    FavoriteVO vo = new FavoriteVO();
                    vo.setId(f.getId());
                    vo.setCreatedAt(f.getCreatedAt());
                    Question question = questionMap.get(f.getQuestionId());
                    QuestionVO questionVO = converter.toVO(question);
                    // 收藏列表里的题目天然都是已收藏状态
                    questionVO.setFavorited(true);
                    vo.setQuestion(questionVO);
                    return vo;
                })
                .toList();
    }

    @Override
    public void add(Long userId, Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == null || question.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在或已下架");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setQuestionId(questionId);
        favorite.setCategoryId(question.getCategoryId());
        // INSERT IGNORE：重复收藏静默忽略，保证幂等
        favoriteMapper.insertIgnore(favorite);
    }

    @Override
    public void remove(Long userId, Long questionId) {
        favoriteMapper.deleteByUserAndQuestion(userId, questionId);
    }
}
