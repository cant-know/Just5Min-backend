package com.example.just5minbackend.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品详情（列表 VO 基础上增加「我的」视角状态）。
 * 游客访问时 owned / equipped 为 null（不代表 false，前端按未登录处理）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MallProductDetailVO extends MallProductVO {

    /** 当前登录用户是否已拥有（有兑换记录）；游客为 null */
    private Boolean owned;

    /** 当前登录用户是否佩戴中；游客为 null */
    private Boolean equipped;
}
