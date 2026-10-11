package com.example.just5minbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信入参。
 */
@Data
public class SendMessageDTO {

    @NotNull(message = "请选择要发送的好友")
    private Long toUserId;

    /** 正文，对齐 message.content VARCHAR(500)；Service 层还会 trim 后校验非空 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息最长 500 个字符")
    private String content;
}
