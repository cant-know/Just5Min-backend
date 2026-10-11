package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.ChatSessionVO;
import com.example.just5minbackend.vo.MessageVO;

import java.util.List;

public interface MessageService {

    /**
     * 与某好友的聊天记录（按时间正序返回，最后一条是最新的）。
     *
     * @param userId   当前登录用户
     * @param friendId 对方（必须是好友，否则抛 1012）
     * @param beforeId 上一页最旧一条的 id，首次拉取传 null
     * @param limit    本页条数，会被收敛到 1~MAX_LIMIT
     */
    List<MessageVO> history(Long userId, Long friendId, Long beforeId, Integer limit);

    /** 发送消息（仅好友之间，自己发给自己抛 1013），返回落库后的消息（带回显时间与 id） */
    MessageVO send(Long userId, Long toUserId, String content);

    /** 会话列表：每个聊过天的好友一条，含最后一条消息与对方发给我的未读数 */
    List<ChatSessionVO> sessions(Long userId);
}
