package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户搜索结果项。
 * <p>relation 取值：friend（已是好友）/ pending_out（我已申请，待对方处理）/
 * pending_in（对方申请了我，可去处理）/ none（陌生人）/ self（就是你自己）。
 * 出于隐私考虑<b>不返回手机号</b>。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendSearchVO {

    private Long userId;

    private String nickname;

    /** 头像（Base64 DataURL），未设置为 null */
    private String avatarUrl;

    private String relation;
}
