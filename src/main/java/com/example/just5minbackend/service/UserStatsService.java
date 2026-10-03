package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.UserStatsVO;

public interface UserStatsService {

    UserStatsVO stats(Long userId);
}
