package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.SendMessageDTO;
import com.example.just5minbackend.service.MessageService;
import com.example.just5minbackend.vo.ChatSessionVO;
import com.example.just5minbackend.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 好友私信聊天。
 *
 * <p>全部接口需登录（{@code /api/messages} 不在游客白名单里，未登录由 AuthInterceptor 直接 401）。
 * 好友关系校验在 Service 层（复用 {@code FriendMapper.exists}），非好友返回 1012。</p>
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 与某好友的聊天记录（按时间正序，最后一条最新）。
     *
     * @param friendId 对方用户ID
     * @param beforeId 翻页游标：上一页最旧一条的 id，首次不传
     * @param limit    本页条数，默认 30、上限 100
     */
    @GetMapping
    public Result<List<MessageVO>> history(@RequestParam Long friendId,
                                           @RequestParam(required = false) Long beforeId,
                                           @RequestParam(required = false) Integer limit) {
        return Result.success(messageService.history(UserContext.require(), friendId, beforeId, limit));
    }

    /** 会话列表：每个聊过天的好友一条，含最后一条消息与对方发来的未读数 */
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> sessions() {
        return Result.success(messageService.sessions(UserContext.require()));
    }

    /** 发送消息（仅好友之间） */
    @PostMapping
    public Result<MessageVO> send(@Valid @RequestBody SendMessageDTO dto) {
        return Result.success(messageService.send(UserContext.require(), dto.getToUserId(), dto.getContent()));
    }
}
