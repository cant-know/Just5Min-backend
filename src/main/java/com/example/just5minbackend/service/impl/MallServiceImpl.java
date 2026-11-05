package com.example.just5minbackend.service.impl;

import com.example.just5minbackend.common.BusinessException;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.entity.ExchangeRecord;
import com.example.just5minbackend.entity.MallProduct;
import com.example.just5minbackend.entity.PointsLog;
import com.example.just5minbackend.entity.User;
import com.example.just5minbackend.mapper.ExchangeRecordMapper;
import com.example.just5minbackend.mapper.MallCategoryMapper;
import com.example.just5minbackend.mapper.MallProductMapper;
import com.example.just5minbackend.mapper.PointsLogMapper;
import com.example.just5minbackend.mapper.UserMapper;
import com.example.just5minbackend.service.DecorationService;
import com.example.just5minbackend.service.MallService;
import com.example.just5minbackend.vo.ExchangeRecordVO;
import com.example.just5minbackend.vo.ExchangeResultVO;
import com.example.just5minbackend.vo.MallCategoryVO;
import com.example.just5minbackend.vo.MallProductDetailVO;
import com.example.just5minbackend.vo.MallProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MallServiceImpl implements MallService {

    private final MallCategoryMapper mallCategoryMapper;
    private final MallProductMapper mallProductMapper;
    private final ExchangeRecordMapper exchangeRecordMapper;
    private final PointsLogMapper pointsLogMapper;
    private final UserMapper userMapper;
    private final DecorationService decorationService;

    @Override
    public List<MallCategoryVO> listCategories(Integer bizType) {
        // 默认 1（资料分类）：文章专栏复用本接口，不能让它看到装扮分类
        int type = bizType == null ? 1 : bizType;
        return mallCategoryMapper.listEnabledByBizType(type).stream()
                .map(c -> new MallCategoryVO(c.getId(), c.getName()))
                .toList();
    }

    @Override
    public List<MallProductVO> listProducts(Long categoryId) {
        return mallProductMapper.listEnabled(categoryId).stream()
                .map(this::toProductVO)
                .toList();
    }

    @Override
    public MallProductDetailVO getProduct(Long productId, Long userId) {
        MallProduct product = mallProductMapper.selectById(productId);
        // 详情按「上架」口径返回；下架/不存在统一 404（与列表口径一致）
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        MallProductDetailVO vo = new MallProductDetailVO();
        copyProduct(product, vo);
        if (userId != null) {
            ExchangeRecord record = exchangeRecordMapper.selectByUserAndProduct(userId, productId);
            boolean owned = record != null;
            vo.setOwned(owned);
            vo.setEquipped(owned && isEquipped(userId, product));
        }
        return vo;
    }

    /**
     * 兑换商品（学习资料 / 虚拟装扮通用）。
     * <p>规则：</p>
     * <ul>
     *   <li><b>装扮（decoration_kind 非空）已拥有</b>：幂等返回原记录，不扣积分/库存，
     *       不改当前佩戴状态（「摘下后再点兑换」不应强制换回）；</li>
     *   <li><b>免费商品（price = 0）</b>：不扣积分、不扣库存，幂等——已领过直接返回原记录；</li>
     *   <li><b>付费商品（price &gt; 0）</b>：先原子扣库存、再原子扣积分，任一失败整体回滚，
     *       防止超兑/透支（只靠条件 UPDATE 的影响行数判断，不做「先查后扣」）；</li>
     *   <li>装扮首次兑换成功后<b>自动佩戴</b>（与兑换同一事务）。</li>
     * </ul>
     */
    @Override
    @Transactional
    public ExchangeResultVO exchange(Long userId, Long productId) {
        MallProduct product = mallProductMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }

        int price = product.getPrice() == null ? 0 : product.getPrice();
        boolean decoration = product.getDecorationKind() != null;
        int balance = readPoints(userId);

        // 装扮幂等：已拥有直接复用（必须在免费分支之前，免费装扮才拿得到 alreadyOwned 语义）
        if (decoration) {
            ExchangeRecord existed = exchangeRecordMapper.selectByUserAndProduct(userId, productId);
            if (existed != null) {
                return new ExchangeResultVO(existed.getId(), product.getId(), product.getName(),
                        null, 0, balance, product.getDecorationKind(), true,
                        isEquipped(userId, product));
            }
        }

        // 免费商品：直接复用已有领取记录（幂等），不产生积分流水
        if (price <= 0) {
            ExchangeRecord existed = exchangeRecordMapper.selectByUserAndProduct(userId, productId);
            if (existed != null) {
                return buildResult(existed, product, 0, balance, false, null);
            }
            ExchangeRecord free = insertRecord(userId, product, 0);
            return afterExchange(userId, free, product, 0, balance, decoration);
        }

        // 1) 原子扣库存（stock > 0 才成功）
        if (mallProductMapper.deductStock(productId) == 0) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH, "手慢了，商品已被兑完");
        }

        // 2) 原子扣积分（points >= price 才成功；失败则上面的扣库存一并回滚）
        if (userMapper.deductPoints(userId, price) == 0) {
            throw new BusinessException(ResultCode.POINTS_NOT_ENOUGH, "积分不足，去刷几道题再来看看吧");
        }

        // 3) 兑换记录（名称/积分均为快照）
        ExchangeRecord record = insertRecord(userId, product, price);

        // 4) 积分流水（事务内可读到刚扣减后的余额）
        int newBalance = readPoints(userId);
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(-price);
        log.setBalance(newBalance);
        log.setSource(2);
        log.setRefId(record.getId());
        log.setRemark("兑换：" + product.getName());
        pointsLogMapper.insert(log);

        return afterExchange(userId, record, product, price, newBalance, decoration);
    }

    /** 兑换落库后的公共收尾：装扮自动佩戴 + 组装返回值 */
    private ExchangeResultVO afterExchange(Long userId, ExchangeRecord record, MallProduct product,
                                           int pointsCost, int balance, boolean decoration) {
        Boolean equipped = null;
        if (decoration) {
            decorationService.autoEquip(userId, product);
            equipped = true;
        }
        return buildResult(record, product, pointsCost, balance, false, equipped);
    }

    private ExchangeResultVO buildResult(ExchangeRecord record, MallProduct product,
                                         int pointsCost, int balance, Boolean alreadyOwned, Boolean equipped) {
        return new ExchangeResultVO(record.getId(), product.getId(), product.getName(),
                product.getArticleId(), pointsCost, balance,
                product.getDecorationKind(), alreadyOwned, equipped);
    }

    /** 用户当前是否佩戴着该商品（任意槽位命中即可） */
    private boolean isEquipped(Long userId, MallProduct product) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        Long pid = product.getId();
        return pid.equals(user.getAvatarFrameId())
                || pid.equals(user.getChatBubbleId())
                || pid.equals(user.getPendantId());
    }

    private ExchangeRecord insertRecord(Long userId, MallProduct product, int pointsCost) {
        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(userId);
        record.setProductId(product.getId());
        record.setProductName(product.getName());
        record.setPointsCost(pointsCost);
        exchangeRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<ExchangeRecordVO> listExchanges(Long userId) {
        return exchangeRecordMapper.listByUser(userId).stream()
                .map(r -> {
                    ExchangeRecordVO vo = new ExchangeRecordVO();
                    vo.setId(r.getId());
                    vo.setProductId(r.getProductId());
                    vo.setProductName(r.getProductName());
                    vo.setPointsCost(r.getPointsCost());
                    vo.setStatus(r.getStatus());
                    vo.setCreatedAt(r.getCreatedAt());
                    return vo;
                })
                .toList();
    }

    private MallProductVO toProductVO(MallProduct p) {
        MallProductVO vo = new MallProductVO();
        copyProduct(p, vo);
        return vo;
    }

    private void copyProduct(MallProduct p, MallProductVO vo) {
        vo.setId(p.getId());
        vo.setCategoryId(p.getCategoryId());
        vo.setName(p.getName());
        vo.setDescription(p.getDescription());
        vo.setCoverUrl(p.getCoverUrl());
        vo.setArticleId(p.getArticleId());
        vo.setPrice(p.getPrice());
        vo.setStock(p.getStock());
        vo.setProductType(p.getProductType());
        vo.setDecorationKind(p.getDecorationKind());
        vo.setDecorationAsset(p.getDecorationAsset());
    }

    private int readPoints(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null || user.getPoints() == null ? 0 : user.getPoints();
    }
}
