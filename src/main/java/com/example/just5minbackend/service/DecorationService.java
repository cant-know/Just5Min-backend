package com.example.just5minbackend.service;

import com.example.just5minbackend.entity.MallProduct;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.vo.DecorationVO;

import java.util.Collection;
import java.util.Map;

/**
 * 装扮（头像框/聊天气泡/主页挂件）的佩戴与展示。
 *
 * <p>「拥有」= exchange_record 里有兑换记录；「佩戴」= user 表对应槽位非空。</p>
 */
public interface DecorationService {

    /** 从用户实体组装佩戴装扮（不查用户行，product 批量回查由实现内部处理） */
    DecorationVO of(User user);

    /** 查当前用户的佩戴装扮（聊天页轻量接口用） */
    DecorationVO equipped(Long userId);

    /** 批量组装（好友列表下发；ids 相同的商品只查一次库） */
    Map<Long, DecorationVO> batch(Collection<User> users);

    /**
     * 佩戴 / 摘下。
     *
     * @param kind      1头像框 2聊天气泡 3主页挂件
     * @param productId 传 null 表示摘下该槽位
     */
    void equip(Long userId, Integer kind, Long productId);

    /**
     * 兑换成功后的自动佩戴（与兑换同一事务）。
     * 与 {@link #equip} 的区别：跳过「已拥有」校验（记录刚插入），商品类型由调用方保证。
     */
    void autoEquip(Long userId, MallProduct product);
}
