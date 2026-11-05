package com.example.just5minbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户当前佩戴的装扮（3 个槽位）。
 *
 * <p>下发的是<b>可直接渲染的值</b>：头像框/挂件为小程序包内透明 PNG 路径，
 * 气泡为主题 key（前端映射 CSS class）。未佩戴的槽位为 null。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecorationVO {

    /** 佩戴中的头像框商品ID，未佩戴为 null */
    private Long avatarFrameId;

    /** 头像框叠加层 PNG 路径（/static/decoration/*.png），未佩戴为 null */
    private String avatarFrameUrl;

    /** 佩戴中的聊天气泡商品ID，未佩戴为 null */
    private Long chatBubbleId;

    /** 气泡主题 key（lime/sakura/ocean/gold），未佩戴为 null */
    private String chatBubbleSkin;

    /** 佩戴中的主页挂件商品ID，未佩戴为 null */
    private Long pendantId;

    /** 挂件叠加层 PNG 路径，未佩戴为 null */
    private String pendantUrl;
}
