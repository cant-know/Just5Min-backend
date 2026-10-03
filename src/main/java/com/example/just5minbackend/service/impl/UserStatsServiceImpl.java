package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.AnswerRecordMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.mapper.WrongQuestionMapper;
import com.example.just5minbackend.service.UserStatsService;
import com.example.just5minbackend.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final AnswerRecordMapper answerRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final UserMapper userMapper;

    @Override
    public UserStatsVO stats(Long userId) {
        long total = answerRecordMapper.countByUser(userId);
        long correct = answerRecordMapper.countCorrectByUser(userId);
        long wrong = wrongQuestionMapper.countByUser(userId);

        User user = userMapper.selectById(userId);

        UserStatsVO vo = new UserStatsVO();
        vo.setTotalAnswered(total);
        vo.setTotalCorrect(correct);
        vo.setWrongCount(wrong);
        vo.setAccuracy(total == 0 ? 0.0 : Math.round(correct * 100.0 / total) / 100.0);
        vo.setPerCategory(answerRecordMapper.statsByCategory(userId));
        vo.setPoints(user == null || user.getPoints() == null ? 0 : user.getPoints());
        return vo;
    }
}
