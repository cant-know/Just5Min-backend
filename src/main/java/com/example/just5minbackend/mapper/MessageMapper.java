package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.Message;
import com.example.just5minbackend.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface MessageMapper {

    /** 插入一条消息，回填自增 id 到实体上（供接口直接回传） */
    int insert(Message message);

    /** 按主键回查（插入后补齐数据库生成的 created_at） */
    Message selectById(@Param("id") Long id);

    /**
     * 会话历史（倒序取 limit 条，供翻页）。
     *
     * <p>两个方向各自走复合索引（idx_from_to / idx_to_from）倒序扫 limit 条再 UNION ALL，
     * 外层再倒序截 limit —— 这样即使聊天记录很长，也只扫索引上相邻的一小段，
     * 不会退化成「整表过滤再排序」。{@code beforeId} 为上一页最旧一条的 id（首次传 null）。</p>
     */
    List<Message> listHistory(@Param("userId") Long userId,
                              @Param("friendId") Long friendId,
                              @Param("beforeId") Long beforeId,
                              @Param("limit") int limit);

    /** 把「对方发给我」的未读全部置已读，返回影响行数（只有收件人能置） */
    int markRead(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 我和某个好友之间的消息总数（用于判断是否已有历史） */
    long countWith(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 每个好友的最后一条消息（会话列表用）。
     *
     * <p>用窗口函数 ROW_NUMBER() 按「会话对」分组取 id 最大的一条；
     * 会话键 = 两用户 id 拼成的字符串（LEAST/GREATEST 保证双向一致）。
     * 只扫 from_user_id = 我 或 to_user_id = 我 的行，两条索引都能命中。</p>
     */
    List<ChatSessionVO> listSessions(@Param("userId") Long userId);

    /** 每个好友发给我的未读条数 */
    List<ChatSessionVO> countUnreadByFriend(@Param("userId") Long userId);

    /** 这批好友中哪些与我聊过（用于会话列表回填） */
    List<Long> listChattedIds(@Param("userId") Long userId,
                              @Param("ids") Collection<Long> ids);
}
