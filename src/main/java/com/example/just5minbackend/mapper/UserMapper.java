package com.example.just5minbackend.mapper;

import com.example.just5minbackend.entity.User;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface UserMapper {

    /** 按微信 openid 查用户（微信登录），含头像 */
    User selectByOpenid(@Param("openid") String openid);

    /** 按手机号查用户（手机号注册/密码登录），含头像 */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 按 ID 查用户。<b>不返回 avatar_url</b>（热路径：答题结算 / 兑换 / 打卡 / 统计都只读 points）。
     * 需要头像请用 {@link #selectProfileById(Long)}。
     */
    User selectById(@Param("id") Long id);

    /** 按 ID 查用户，含头像（「我的」资料页用） */
    User selectProfileById(@Param("id") Long id);

    /** 批量查用户最小字段（id/nickname/avatar_url/points），用于好友列表与搜索 */
    List<User> selectBriefByIds(@Param("ids") Collection<Long> ids);

    /** 昵称模糊搜索（keyword 需由调用方转义 ! % _），仅返回启用用户 */
    List<User> searchByNicknameLike(@Param("keyword") String keyword, @Param("limit") int limit);

    int insert(User user);

    /**
     * 编辑资料：动态更新，只改非 null 字段。
     *
     * <p>调用方必须保证至少有一个字段需要更新（否则 SET 为空会生成非法 SQL）。
     * 文本字段（昵称/签名/地区/院校/专业）传空串表示<b>清空</b>，传 null 表示<b>不动</b>；
     * {@code birthday} / {@code examYear} 用 {@code clearXxx} 开关显式表达清空。</p>
     */
    int updateProfile(@Param("userId") Long userId,
                      @Param("nickname") String nickname,
                      @Param("avatarUrl") String avatarUrl,
                      @Param("signature") String signature,
                      @Param("gender") Integer gender,
                      @Param("region") String region,
                      @Param("targetSchool") String targetSchool,
                      @Param("targetMajor") String targetMajor,
                      @Param("birthday") LocalDate birthday,
                      @Param("clearBirthday") boolean clearBirthday,
                      @Param("examYear") Integer examYear,
                      @Param("clearExamYear") boolean clearExamYear);

    /** 加积分（答题结算），返回影响行数 */
    int addPoints(@Param("userId") Long userId, @Param("delta") int delta);

    /** 条件扣积分：points >= amount 才扣，返回影响行数（0 = 积分不足） */
    int deductPoints(@Param("userId") Long userId, @Param("amount") int amount);
}
