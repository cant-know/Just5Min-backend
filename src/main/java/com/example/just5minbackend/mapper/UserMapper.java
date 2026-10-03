package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    /** 按微信 openid 查用户（微信登录） */
    User selectByOpenid(@Param("openid") String openid);

    /** 按手机号查用户（手机号注册/密码登录） */
    User selectByPhone(@Param("phone") String phone);

    User selectById(@Param("id") Long id);

    int insert(User user);

    /** 加积分（答题结算），返回影响行数 */
    int addPoints(@Param("userId") Long userId, @Param("delta") int delta);

    /** 条件扣积分：points >= amount 才扣，返回影响行数（0 = 积分不足） */
    int deductPoints(@Param("userId") Long userId, @Param("amount") int amount);
}
