package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.FriendRequest;
import com.example.just5minbackend.vo.FriendRequestVO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface FriendRequestMapper {

    /** 按 (from, to) 查请求（唯一键保证至多一条） */
    FriendRequest selectByFromTo(@Param("fromUserId") Long fromUserId,
                                 @Param("toUserId") Long toUserId);

    FriendRequest selectById(@Param("id") Long id);

    /**
     * 发起/重新发起请求。
     * uk_from_to 冲突时：已拒绝(2) 复位为待处理(0)，其它状态保持不变（幂等）。
     * 返回影响行数（1=新插入 / 2=更新为待处理 / 0=已是待处理，值未变）
     */
    int upsertRequest(@Param("fromUserId") Long fromUserId,
                      @Param("toUserId") Long toUserId,
                      @Param("message") String message);

    /**
     * 处理请求（同意/拒绝）。条件更新保证：只能由收件人处理、且只能处理待处理状态。
     * 返回影响行数，0 表示请求不存在 / 不属于当前用户 / 已被处理。
     */
    int markHandled(@Param("id") Long id,
                    @Param("toUserId") Long toUserId,
                    @Param("status") int status);

    /** 我收到的待处理请求（join 发起人信息） */
    List<FriendRequestVO> listIncoming(@Param("toUserId") Long toUserId);

    /** 我收到的待处理请求数量（tabBar 角标） */
    int countPending(@Param("toUserId") Long toUserId);

    /** 这批候选用户中哪些向我发过待处理请求（搜索结果 relation=pending_in） */
    List<Long> listPendingInIds(@Param("toUserId") Long toUserId,
                                @Param("ids") Collection<Long> ids);

    /** 我向这批候选用户中哪些发过待处理请求（搜索结果 relation=pending_out） */
    List<Long> listPendingOutIds(@Param("fromUserId") Long fromUserId,
                                 @Param("ids") Collection<Long> ids);
}
