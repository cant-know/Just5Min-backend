package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.Message;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.FriendMapper;
import com.example.just5minbackend.mapper.MessageMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.MessageService;
import com.example.just5minbackend.vo.ChatSessionVO;
import com.example.just5minbackend.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 好友私信：历史 / 发送 / 会话列表。
 *
 * <p><b>权限模型</b>：消息只在好友之间流转。每个入口都先过
 * {@link FriendMapper#exists}，非好友一律 1012 —— 这样即使前端被改，
 * 也无法给陌生人发消息（A-B 的 friend 行被删除后，双方立刻发不出消息）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    /** 聊天页单页消息条数：默认 30（够一屏多），上限 100（再大就分页拉） */
    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;

    /** 会话列表最多返回多少个好友 */
    private static final int MAX_SESSIONS = 100;

    private final MessageMapper messageMapper;
    private final FriendMapper friendMapper;
    private final UserMapper userMapper;

    // ------------------------------------------------------------------ 历史

    @Override
    @Transactional
    public List<MessageVO> history(Long userId, Long friendId, Long beforeId, Integer limit) {
        requireFriend(userId, friendId);

        // Mapper 倒序取 limit 条（顺带用 beforeId 翻页），这里翻成正序给前端直接渲染
        List<Message> rows = messageMapper.listHistory(userId, friendId, beforeId, normalizeLimit(limit));
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<MessageVO> result = new ArrayList<>(rows.size());
        // rows 是倒序的（最新在前），倒着遍历即得到正序
        for (int i = rows.size() - 1; i >= 0; i--) {
            result.add(toVO(rows.get(i), userId));
        }

        // 进会话即视为已读：把对方发来的未读批量置 1。
        // 放在读之后，这样上面查出来的历史里 is_read 仍是「读之前」的真实状态。
        messageMapper.markRead(userId, friendId);
        return result;
    }

    // ------------------------------------------------------------------ 发送

    @Override
    @Transactional
    public MessageVO send(Long userId, Long toUserId, String content) {
        if (toUserId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择要发送的好友");
        }
        if (userId.equals(toUserId)) {
            throw new BusinessException(ResultCode.CANNOT_MESSAGE_SELF);
        }
        requireFriend(userId, toUserId);

        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "消息内容不能为空");
        }
        // 兜一层长度（@Size 已校验，但 trim 后的实际长度以这里为准，DB 列是 VARCHAR(500)）
        if (text.length() > 500) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "消息最长 500 个字符");
        }

        Message message = new Message();
        message.setFromUserId(userId);
        message.setToUserId(toUserId);
        message.setContent(text);
        // 插入后 useGeneratedKeys 会把 id 回填到实体上
        messageMapper.insert(message);

        // created_at 由数据库默认值生成，插入时不在实体里，回查一次补齐，
        // 否则接口返回 createdAt=null，前端刚发出的气泡缺时间分隔。
        Message saved = message.getId() == null ? message : messageMapper.selectById(message.getId());
        return toVO(saved == null ? message : saved, userId);
    }

    // ------------------------------------------------------------------ 会话

    @Override
    public List<ChatSessionVO> sessions(Long userId) {
        List<ChatSessionVO> raw = messageMapper.listSessions(userId);
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        if (raw.size() > MAX_SESSIONS) {
            raw = raw.subList(0, MAX_SESSIONS);
        }

        // 批量补昵称/头像 + 未读数，避免逐个会话查库
        List<Long> peerIds = raw.stream().map(ChatSessionVO::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBriefByIds(peerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        Map<Long, Long> unreadMap = messageMapper.countUnreadByFriend(userId).stream()
                .filter(v -> v.getUserId() != null)
                .collect(Collectors.toMap(ChatSessionVO::getUserId,
                        v -> v.getUnreadCount() == null ? 0L : v.getUnreadCount(), (a, b) -> a));

        List<ChatSessionVO> result = new ArrayList<>(raw.size());
        for (ChatSessionVO session : raw) {
            User peer = userMap.get(session.getUserId());
            session.setNickname(displayName(peer, session.getUserId()));
            session.setAvatarUrl(peer == null ? null : peer.getAvatarUrl());
            session.setUnreadCount(unreadMap.getOrDefault(session.getUserId(), 0L));
            result.add(session);
        }
        return result;
    }

    // ------------------------------------------------------------------ 工具

    /** 好友关系校验：非好友不能收发消息 */
    private void requireFriend(Long userId, Long friendId) {
        if (friendId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少好友ID");
        }
        if (friendMapper.exists(userId, friendId) <= 0) {
            throw new BusinessException(ResultCode.NOT_FRIEND);
        }
    }

    private MessageVO toVO(Message message, Long userId) {
        boolean mine = userId.equals(message.getFromUserId());
        return new MessageVO(
                message.getId(),
                message.getFromUserId(),
                message.getToUserId(),
                message.getContent(),
                mine,
                // 「已读」只对我发出的消息有意义；对方发来的消息不必把已读状态暴露给发送方以外的人
                mine ? Integer.valueOf(1).equals(message.getIsRead()) : null,
                message.getCreatedAt()
        );
    }

    private String displayName(User user, Long fallbackId) {
        if (user == null || user.getNickname() == null || user.getNickname().isBlank()) {
            return "用户" + fallbackId;
        }
        return user.getNickname();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
