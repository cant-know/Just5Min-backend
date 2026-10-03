package com.example.just5minbackend.support;

import com.example.just5minbackend.entity.Question;
import com.example.just5minbackend.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Question(实体, options 为 JSON 字符串) → QuestionVO(options 为 Map) 的转换器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionVoConverter {

    private static final TypeReference<LinkedHashMap<String, String>> OPTIONS_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    /**
     * 默认不下发答案与解析。
     */
    public QuestionVO toVO(Question question) {
        return toVO(question, false);
    }

    public QuestionVO toVO(Question question, boolean withAnswer) {
        if (question == null) {
            return null;
        }
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setCategoryId(question.getCategoryId());
        vo.setQuestionType(question.getQuestionType());
        vo.setContent(question.getContent());
        vo.setOptions(parseOptions(question.getOptions()));
        vo.setDifficulty(question.getDifficulty());
        if (withAnswer) {
            vo.setAnswer(question.getAnswer());
            vo.setAnalysis(question.getAnalysis());
        }
        return vo;
    }

    public Map<String, String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, OPTIONS_TYPE);
        } catch (Exception e) {
            log.warn("题目 options 不是合法 JSON: {}", json);
            return Collections.emptyMap();
        }
    }
}
