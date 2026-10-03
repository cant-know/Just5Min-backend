package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.entity.Question;
import com.example.just5minbackend.entity.WrongQuestion;
import com.example.just5minbackend.mapper.QuestionMapper;
import com.example.just5minbackend.mapper.WrongQuestionMapper;
import com.example.just5minbackend.service.WrongQuestionService;
import com.example.just5minbackend.support.QuestionVoConverter;
import com.example.just5minbackend.vo.WrongQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionVoConverter converter;

    @Override
    public List<WrongQuestionVO> list(Long userId, Long categoryId) {
        List<WrongQuestion> wrongs = wrongQuestionMapper.listByUser(userId, categoryId);
        if (wrongs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = wrongs.stream().map(WrongQuestion::getQuestionId).toList();
        Map<Long, Question> questionMap = questionMapper.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));

        return wrongs.stream()
                .filter(w -> questionMap.containsKey(w.getQuestionId()))
                .map(w -> {
                    WrongQuestionVO vo = new WrongQuestionVO();
                    vo.setWrongCount(w.getWrongCount());
                    vo.setLastWrongAnswer(w.getLastWrongAnswer());
                    vo.setLastWrongAt(w.getLastWrongAt());
                    vo.setQuestion(converter.toVO(questionMap.get(w.getQuestionId())));
                    return vo;
                })
                .toList();
    }

    @Override
    public void remove(Long userId, Long questionId) {
        wrongQuestionMapper.deleteByUserAndQuestion(userId, questionId);
    }
}
