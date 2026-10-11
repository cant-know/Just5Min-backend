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
import com.example.just5minbackend.service.MallService;
import com.example.just5minbackend.vo.ExchangeRecordVO;
import com.example.just5minbackend.vo.ExchangeResultVO;
import com.example.just5minbackend.vo.MallCategoryVO;
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

    @Override
    public List<MallCategoryVO> listCategories() {
        return mallCategoryMapper.listAllEnabled().stream()
                .map(c -> new MallCategoryVO(c.getId(), c.getName()))
                .toList();
    }

    @Override
    public List<MallProductVO> listProducts(Long categoryId) {
        return mallProductMapper.listEnabled(categoryId).stream()
                .map(this::toProductVO)
                .toList();
    }

    /**
     * 领取/解锁学习资料。
     * <p>规则：</p>
     * <ul>
     *   <li><b>免费资料（price = 0）</b>：不扣积分、不扣库存，幂等——已领过直接返回原记录；</li>
     *   <li><b>付费商品（price &gt; 0）</b>：先原子扣库存、再原子扣积分，任一失败整体回滚，
     *       防止超兑/透支（只靠条件 UPDATE 的影响行数判断，不做「先查后扣」）。</li>
     * </ul>
     */
    @Override
    @Transactional
    public ExchangeResultVO exchange(Long userId, Long productId) {
        MallProduct product = mallProductMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资料不存在或已下架");
        }

        int price = product.getPrice() == null ? 0 : product.getPrice();
        int balance = readPoints(userId);

        // 免费资料：直接复用已有领取记录（幂等），不产生积分流水
        if (price <= 0) {
            ExchangeRecord existed = exchangeRecordMapper.selectByUserAndProduct(userId, productId);
            if (existed != null) {
                return new ExchangeResultVO(existed.getId(), product.getId(), product.getName(),
                        product.getArticleId(), 0, balance);
            }
            ExchangeRecord free = insertRecord(userId, product, 0);
            return new ExchangeResultVO(free.getId(), product.getId(), product.getName(),
                    product.getArticleId(), 0, balance);
        }

        // 1) 原子扣库存（stock > 0 才成功）
        if (mallProductMapper.deductStock(productId) == 0) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH, "手慢了，资料已被领完");
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

        return new ExchangeResultVO(record.getId(), product.getId(), product.getName(),
                product.getArticleId(), price, newBalance);
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
        vo.setId(p.getId());
        vo.setCategoryId(p.getCategoryId());
        vo.setName(p.getName());
        vo.setDescription(p.getDescription());
        vo.setCoverUrl(p.getCoverUrl());
        vo.setArticleId(p.getArticleId());
        vo.setPrice(p.getPrice());
        vo.setStock(p.getStock());
        vo.setProductType(p.getProductType());
        return vo;
    }

    private int readPoints(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null || user.getPoints() == null ? 0 : user.getPoints();
    }
}
