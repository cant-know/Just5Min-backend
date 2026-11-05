package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.MallProduct;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.ExchangeRecordMapper;
import com.example.just5minbackend.mapper.MallProductMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.DecorationService;
import com.example.just5minbackend.vo.DecorationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DecorationServiceImpl implements DecorationService {

    /** 装扮类型：与 mall_product.decoration_kind 及 user 表三个槽位一一对应 */
    public static final int KIND_AVATAR_FRAME = 1;
    public static final int KIND_CHAT_BUBBLE = 2;
    public static final int KIND_PENDANT = 3;

    private final UserMapper userMapper;
    private final MallProductMapper mallProductMapper;
    private final ExchangeRecordMapper exchangeRecordMapper;

    @Override
    public DecorationVO of(User user) {
        if (user == null) {
            return empty();
        }
        Set<Long> ids = collectSlotIds(user);
        if (ids.isEmpty()) {
            return empty();
        }
        // 一次批量回查装扮资源；IN 空集合已短路，这里必有元素
        Map<Long, MallProduct> products = new HashMap<>();
        for (MallProduct p : mallProductMapper.listByIds(ids)) {
            products.put(p.getId(), p);
        }
        return build(user.getAvatarFrameId(), user.getChatBubbleId(), user.getPendantId(), products);
    }

    @Override
    public DecorationVO equipped(Long userId) {
        User user = userMapper.selectById(userId);
        return of(user);
    }

    @Override
    public Map<Long, DecorationVO> batch(Collection<User> users) {
        if (users == null || users.isEmpty()) {
            return Map.of();
        }
        // 收集所有非空槽位 id，去重后一次查库
        Set<Long> allIds = new HashSet<>();
        for (User u : users) {
            allIds.addAll(collectSlotIds(u));
        }
        Map<Long, MallProduct> products = new HashMap<>();
        if (!allIds.isEmpty()) {
            for (MallProduct p : mallProductMapper.listByIds(allIds)) {
                products.put(p.getId(), p);
            }
        }
        Map<Long, DecorationVO> result = new LinkedHashMap<>();
        for (User u : users) {
            result.put(u.getId(), build(u.getAvatarFrameId(), u.getChatBubbleId(), u.getPendantId(), products));
        }
        return result;
    }

    @Override
    public void equip(Long userId, Integer kind, Long productId) {
        if (kind == null || kind < KIND_AVATAR_FRAME || kind > KIND_PENDANT) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "装扮类型不合法");
        }
        if (productId == null) {
            // 摘下：直接清槽位
            updateSlot(userId, kind, null);
            return;
        }
        MallProduct product = mallProductMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        if (product.getDecorationKind() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该商品不是装扮");
        }
        if (product.getDecorationKind() != kind) {
            throw new BusinessException(ResultCode.DECORATION_TYPE_MISMATCH);
        }
        // 必须已拥有（有兑换记录）
        if (exchangeRecordMapper.selectByUserAndProduct(userId, productId) == null) {
            throw new BusinessException(ResultCode.DECORATION_NOT_OWNED);
        }
        updateSlot(userId, kind, productId);
    }

    @Override
    public void autoEquip(Long userId, MallProduct product) {
        if (product == null || product.getDecorationKind() == null) {
            return;
        }
        updateSlot(userId, product.getDecorationKind(), product.getId());
    }

    private void updateSlot(Long userId, Integer kind, Long productId) {
        switch (kind) {
            case KIND_AVATAR_FRAME -> userMapper.updateAvatarFrame(userId, productId);
            case KIND_CHAT_BUBBLE -> userMapper.updateChatBubble(userId, productId);
            case KIND_PENDANT -> userMapper.updatePendant(userId, productId);
            default -> throw new BusinessException(ResultCode.PARAM_ERROR, "装扮类型不合法");
        }
    }

    /** 收集一个用户 3 个槽位里非空的商品 id */
    private Set<Long> collectSlotIds(User user) {
        Set<Long> ids = new HashSet<>();
        if (user.getAvatarFrameId() != null) ids.add(user.getAvatarFrameId());
        if (user.getChatBubbleId() != null) ids.add(user.getChatBubbleId());
        if (user.getPendantId() != null) ids.add(user.getPendantId());
        return ids;
    }

    private DecorationVO build(Long frameId, Long bubbleId, Long pendantId, Map<Long, MallProduct> products) {
        String frameUrl = assetOf(frameId, products);
        String bubbleSkin = assetOf(bubbleId, products);
        String pendantUrl = assetOf(pendantId, products);
        if (frameUrl == null && bubbleSkin == null && pendantUrl == null) {
            return empty();
        }
        return new DecorationVO(frameId, frameUrl, bubbleId, bubbleSkin, pendantId, pendantUrl);
    }

    /** 取装扮资源值；商品不存在/非装扮时返回 null（降级为未佩戴展示） */
    private String assetOf(Long productId, Map<Long, MallProduct> products) {
        if (productId == null) {
            return null;
        }
        MallProduct p = products.get(productId);
        return p == null ? null : p.getDecorationAsset();
    }

    private DecorationVO empty() {
        return new DecorationVO(null, null, null, null, null, null);
    }
}
