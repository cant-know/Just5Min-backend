package com.example.just5minbackend.service;

import com.example.just5minbackend.vo.FriendRequestVO;
import com.example.just5minbackend.vo.FriendSearchVO;
import com.example.just5minbackend.vo.FriendVO;

import java.util.List;

public interface FriendService {

    /* 搜索结果里的关系标记 */
    String RELATION_SELF = "self";
    String RELATION_FRIEND = "friend";
    String RELATION_PENDING_OUT = "pending_out";
    String RELATION_PENDING_IN = "pending_in";
    String RELATION_NONE = "none";

    /** 好友列表（含对方学习数据），limit 会被收敛到 1~MAX_PAGE_SIZE */
    List<FriendVO> list(Long userId, Integer limit, Integer offset);

    /** 删除好友（双向） */
    void remove(Long userId, Long friendId);

    /** 搜索用户：支持 用户ID / 手机号 精确 与 昵称 模糊 */
    List<FriendSearchVO> search(Long userId, String keyword);

    /** 我收到的待处理好友请求 */
    List<FriendRequestVO> listIncoming(Long userId);

    /** 待处理请求数（tabBar 角标） */
    int pendingCount(Long userId);

    /** 发送好友请求（已是好友/自己/重复发起等边界在实现里处理） */
    void sendRequest(Long fromUserId, Long toUserId, String message);

    /** 同意好友请求（幂等；只能由收件人操作） */
    void accept(Long userId, Long requestId);

    /** 拒绝好友请求（幂等；只能由收件人操作） */
    void reject(Long userId, Long requestId);
}
