package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.Friend;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface FriendMapper {

    /** 建立单向关系（INSERT IGNORE，重复静默忽略），返回影响行数 */
    int insertIgnore(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 删除单向关系 */
    int deleteRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 是否存在好友关系（返回 0/1） */
    int exists(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 我的好友关系（按成为好友时间倒序分页） */
    List<Friend> listFriends(@Param("userId") Long userId,
                             @Param("limit") int limit,
                             @Param("offset") int offset);

    long countFriends(@Param("userId") Long userId);

    /** 这批候选用户中哪些已经是我的好友（用于搜索结果回填关系） */
    List<Long> listFriendIdsAmong(@Param("userId") Long userId,
                                  @Param("ids") Collection<Long> ids);
}
