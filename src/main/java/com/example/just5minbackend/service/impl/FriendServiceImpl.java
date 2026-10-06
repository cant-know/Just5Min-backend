package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.Friend;
import com.example.just5minbackend.entity.FriendRequest;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.AnswerRecordMapper;
import com.example.just5minbackend.mapper.CheckInMapper;
import com.example.just5minbackend.mapper.FriendMapper;
import com.example.just5minbackend.mapper.FriendRequestMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.FriendService;
import com.example.just5minbackend.vo.FriendRequestVO;
import com.example.just5minbackend.vo.FriendSearchVO;
import com.example.just5minbackend.vo.FriendVO;
import com.example.just5minbackend.vo.UserAnswerStatVO;
import com.example.just5minbackend.vo.UserCheckInStatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 好友：列表 / 搜索 / 请求 / 同意拒绝。
 *
 * <p>关系模型：{@code friend} 双向各存一行；{@code friend_request} 单方向一行（uk_from_to）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    /** 手机号精确匹配（与登录表单同一套规则） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 昵称模糊搜索结果上限 */
    private static final int SEARCH_LIMIT = 20;

    /** 好友列表页大小：默认 50，最大 50（控制响应体，头像为 Base64） */
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 50;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_ACCEPTED = 1;
    private static final int STATUS_REJECTED = 2;

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final UserMapper userMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final CheckInMapper checkInMapper;

    // ------------------------------------------------------------------ 列表

    @Override
    public List<FriendVO> list(Long userId, Integer limit, Integer offset) {
        List<Friend> relations = friendMapper.listFriends(userId, normalizeLimit(limit), normalizeOffset(offset));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        return buildVOs(relations);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long friendId) {
        // 双向删除，保证两边都看不到对方
        friendMapper.deleteRelation(userId, friendId);
        friendMapper.deleteRelation(friendId, userId);
    }

    private List<FriendVO> buildVOs(List<Friend> relations) {
        List<Long> ids = relations.stream().map(Friend::getFriendId).distinct().toList();

        Map<Long, User> userMap = userMapper.selectBriefByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        // 无答题记录 / 无打卡记录的用户不会出现在聚合结果里，下面按 id 遍历并补 0
        Map<Long, UserAnswerStatVO> answerMap = answerRecordMapper.statsByUsers(ids).stream()
                .collect(Collectors.toMap(UserAnswerStatVO::getUserId, Function.identity(), (a, b) -> a));
        Map<Long, UserCheckInStatVO> checkinMap = checkInMapper.countByUsers(ids).stream()
                .collect(Collectors.toMap(UserCheckInStatVO::getUserId, Function.identity(), (a, b) -> a));

        List<FriendVO> result = new ArrayList<>(relations.size());
        for (Friend relation : relations) {
            Long fid = relation.getFriendId();
            User user = userMap.get(fid);

            long total = 0L;
            long correct = 0L;
            UserAnswerStatVO answerStat = answerMap.get(fid);
            if (answerStat != null) {
                total = answerStat.getTotalAnswered() == null ? 0L : answerStat.getTotalAnswered();
                correct = answerStat.getTotalCorrect() == null ? 0L : answerStat.getTotalCorrect();
            }
            UserCheckInStatVO checkinStat = checkinMap.get(fid);
            long days = checkinStat == null || checkinStat.getDays() == null ? 0L : checkinStat.getDays();

            FriendVO vo = new FriendVO();
            vo.setUserId(fid);
            vo.setNickname(displayName(user, fid));
            vo.setAvatarUrl(user == null ? null : user.getAvatarUrl());
            vo.setTotalAnswered(total);
            vo.setAccuracy(total == 0 ? 0.0 : Math.round(correct * 100.0 / total) / 100.0);
            vo.setCheckinDays(days);
            vo.setPoints(user == null || user.getPoints() == null ? 0 : user.getPoints());
            vo.setFriendSince(relation.getCreatedAt());
            result.add(vo);
        }
        return result;
    }

    // ------------------------------------------------------------------ 搜索

    @Override
    public List<FriendSearchVO> search(Long userId, String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> candidates = new ArrayList<>();
        if (PHONE_PATTERN.matcher(kw).matches()) {
            // 完整手机号 → 精确匹配（微信号用户没有手机号，自然查不到）
            User byPhone = userMapper.selectByPhone(kw);
            if (byPhone != null && isEnabled(byPhone)) {
                candidates.add(byPhone);
            }
        } else if (isAllDigits(kw)) {
            // 纯数字 → 当作用户ID 精确匹配
            Long id = parseLongOrNull(kw);
            if (id != null) {
                candidates.addAll(userMapper.selectBriefByIds(List.of(id)));
            }
        } else {
            // 其它 → 昵称模糊（转义 ! % _，避免用户输入的 % 变成通配符）
            candidates.addAll(userMapper.searchByNicknameLike(escapeLike(kw), SEARCH_LIMIT));
        }

        // 去重 + 只保留启用用户
        Map<Long, User> candidateMap = candidates.stream()
                .filter(this::isEnabled)
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        Set<Long> ids = candidateMap.keySet();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        // 一次性批量算出三组关系，避免逐个候选人查库
        Set<Long> friendIds = new HashSet<>(friendMapper.listFriendIdsAmong(userId, ids));
        Set<Long> pendingOut = new HashSet<>(friendRequestMapper.listPendingOutIds(userId, ids));
        Set<Long> pendingIn = new HashSet<>(friendRequestMapper.listPendingInIds(userId, ids));

        List<FriendSearchVO> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            if (id.equals(userId)) {
                // 搜到自己：保留在结果里并标记，前端提示「这是你自己」，比空列表更友好
                result.add(new FriendSearchVO(id, "我自己", candidateMap.get(id).getAvatarUrl(), RELATION_SELF));
                continue;
            }
            String relation = RELATION_NONE;
            if (friendIds.contains(id)) {
                relation = RELATION_FRIEND;
            } else if (pendingOut.contains(id)) {
                relation = RELATION_PENDING_OUT;
            } else if (pendingIn.contains(id)) {
                relation = RELATION_PENDING_IN;
            }
            result.add(new FriendSearchVO(id, displayName(candidateMap.get(id), id),
                    candidateMap.get(id).getAvatarUrl(), relation));
        }
        // 关系优先级排序，让「可添加」的排前面
        result.sort((a, b) -> Integer.compare(relationWeight(a.getRelation()), relationWeight(b.getRelation())));
        return result;
    }

    // ------------------------------------------------------------------ 请求

    @Override
    public List<FriendRequestVO> listIncoming(Long userId) {
        return friendRequestMapper.listIncoming(userId);
    }

    @Override
    public int pendingCount(Long userId) {
        return friendRequestMapper.countPending(userId);
    }

    @Override
    @Transactional
    public void sendRequest(Long fromUserId, Long toUserId, String message) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(ResultCode.CANNOT_ADD_SELF);
        }
        if (userMapper.selectById(toUserId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该用户不存在");
        }
        if (friendMapper.exists(fromUserId, toUserId) > 0) {
            throw new BusinessException(ResultCode.ALREADY_FRIENDS);
        }

        // 对方已经先申请过我 → 视为互相确认，直接成为好友（比提示「请去请求列表处理」更顺）
        FriendRequest reverse = friendRequestMapper.selectByFromTo(toUserId, fromUserId);
        if (reverse != null && isStatus(reverse, STATUS_PENDING)) {
            friendRequestMapper.markHandled(reverse.getId(), fromUserId, STATUS_ACCEPTED);
            makeFriends(fromUserId, toUserId);
            log.info("互相申请，直接建立好友关系 {} <-> {}", fromUserId, toUserId);
            return;
        }

        FriendRequest existing = friendRequestMapper.selectByFromTo(fromUserId, toUserId);
        if (existing != null && isStatus(existing, STATUS_PENDING)) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_SENT);
        }

        // 不存在 → 新建；已被拒绝(status=2) → upsert 复位为待处理
        friendRequestMapper.upsertRequest(fromUserId, toUserId, normalizeMessage(message));
    }

    @Override
    @Transactional
    public void accept(Long userId, Long requestId) {
        int affected = friendRequestMapper.markHandled(requestId, userId, STATUS_ACCEPTED);
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (affected == 0) {
            // 幂等：已经同意过、且关系已建立 → 静默成功；其余（不存在/不属于我/已拒绝）报错
            if (request != null && request.getToUserId().equals(userId)
                    && isStatus(request, STATUS_ACCEPTED)
                    && friendMapper.exists(userId, request.getFromUserId()) > 0) {
                return;
            }
            throw new BusinessException(ResultCode.FRIEND_REQUEST_HANDLED);
        }
        makeFriends(userId, request.getFromUserId());
    }

    @Override
    @Transactional
    public void reject(Long userId, Long requestId) {
        int affected = friendRequestMapper.markHandled(requestId, userId, STATUS_REJECTED);
        if (affected == 0) {
            FriendRequest request = friendRequestMapper.selectById(requestId);
            // 幂等：已经拒绝过 → 静默成功
            if (request != null && request.getToUserId().equals(userId) && isStatus(request, STATUS_REJECTED)) {
                return;
            }
            throw new BusinessException(ResultCode.FRIEND_REQUEST_HANDLED);
        }
    }

    private void makeFriends(Long a, Long b) {
        friendMapper.insertIgnore(a, b);
        friendMapper.insertIgnore(b, a);
    }

    // ------------------------------------------------------------------ 工具

    private String displayName(User user, Long fallbackId) {
        if (user == null || user.getNickname() == null || user.getNickname().isBlank()) {
            return "用户" + fallbackId;
        }
        return user.getNickname();
    }

    private boolean isEnabled(User user) {
        return user != null && (user.getStatus() == null || user.getStatus() == 1);
    }

    private boolean isStatus(FriendRequest request, int status) {
        return request.getStatus() != null && request.getStatus() == status;
    }

    private boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    private Long parseLongOrNull(String value) {
        // 用户可能输入超长数字，Long 解析会溢出，这里兜底成「没有结果」
        if (value.length() > 18) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 配合 SQL 里的 ESCAPE '!' */
    private String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return null;
        }
        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 64) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证附言最长 64 个字符");
        }
        return trimmed;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(limit, MAX_PAGE_SIZE);
    }

    private int normalizeOffset(Integer offset) {
        return offset == null || offset < 0 ? 0 : offset;
    }

    private int relationWeight(String relation) {
        if (relation == null) {
            return 9;
        }
        return switch (relation) {
            case RELATION_NONE -> 0;
            case RELATION_PENDING_IN -> 1;
            case RELATION_PENDING_OUT -> 2;
            case RELATION_FRIEND -> 3;
            default -> 4;
        };
    }
}
