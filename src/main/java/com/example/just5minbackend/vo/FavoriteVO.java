package com.example.just5minbackend.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏条目返回（我的收藏页）。
 */
@Data
public class FavoriteVO {

    private Long id;
    private LocalDateTime createdAt;
    private QuestionVO question;
}
