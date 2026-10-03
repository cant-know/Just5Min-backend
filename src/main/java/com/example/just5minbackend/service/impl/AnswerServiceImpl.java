package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.dto.SubmitAnswerDTO;
import com.example.just5minbackend.entity.AnswerRecord;
import com.example.just5minbackend.entity.PointsLog;
import com.example.just5minbackend.entity.Question;
import com.example.just5minbackend.entity.WrongQuestion;
import com.example.just5minbackend.mapper.AnswerRecordMapper;
import com.example.just5minbackend.mapper.PointsLogMapper;
import com.example.just5minbackend.mapper.QuestionMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.mapper.WrongQuestionMapper;
import com.example.just5minbackend.service.AnswerService;
import com.example.just5minbackend.util.AnswerUtil;
import com.example.just5minbackend.vo.CheckResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final QuestionMapper questionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;

    @Override
    @Transactional
    public CheckResultVO submit(Long userId, SubmitAnswerDTO dto) {
        Question question = questionMapper.selectById(dto.getQuestionId());
        if (question == null || question.getStatus() == null || question.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在或已下架");
        }

        String userAnswer = AnswerUtil.normalize(dto.getUserAnswer());
        if (userAnswer.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "答案内容不合法");
        }
        String correctAnswer = AnswerUtil.normalize(question.getAnswer());
        boolean correct = userAnswer.equals(correctAnswer);

        AnswerRecord record = new AnswerRecord();
        record.setUserId(userId);
        record.setQuestionId(question.getId());
        record.setCategoryId(question.getCategoryId());
        record.setUserAnswer(userAnswer);
        record.setIsCorrect(correct);
        record.setDurationMs(dto.getDurationMs());
        answerRecordMapper.insert(record);

        if (!correct) {
            WrongQuestion wrongQuestion = new WrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setQuestionId(question.getId());
            wrongQuestion.setCategoryId(question.getCategoryId());
            wrongQuestion.setLastWrongAnswer(userAnswer);
            wrongQuestion.setLastWrongAt(LocalDateTime.now());
            wrongQuestionMapper.upsert(wrongQuestion);
        }

        // 积分结算：每提交一次答案 +1（答错也算），原子自增并记流水
        userMapper.addPoints(userId, 1);
        int pointsTotal = userMapper.selectById(userId).getPoints();
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(1);
        log.setBalance(pointsTotal);
        log.setSource(1);
        log.setRefId(record.getId());
        log.setRemark(correct ? "答题正确 +1" : "答题 +1");
        pointsLogMapper.insert(log);

        CheckResultVO vo = new CheckResultVO();
        vo.setCorrect(correct);
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setPointsEarned(1);
        vo.setPointsTotal(pointsTotal);
        return vo;
    }
}
