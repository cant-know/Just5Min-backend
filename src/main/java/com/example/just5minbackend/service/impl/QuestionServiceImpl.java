package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.Question;
import com.example.just5minbackend.mapper.QuestionMapper;
import com.example.just5minbackend.service.QuestionService;
import com.example.just5minbackend.support.QuestionVoConverter;
import com.example.just5minbackend.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final QuestionMapper questionMapper;
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

        return questions.stream()
                .map(converter::toVO)
                .toList();
    }

    @Override
    public QuestionVO getQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null || question.getStatus() == null || question.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在或已下架");
        }
        return converter.toVO(question);
    }
}
