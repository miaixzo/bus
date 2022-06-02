package org.aoju.bus.pay.provider.wxpay.models;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * V3 统一下单-单品列表
 */
@Data
@Accessors(chain = true)
public class GoodsDetail {

    /**
     * 商户侧商品编码
     */
    private String merchant_goods_id;
    /**
     * 微信侧商品编码
     */
    private String wechatpay_goods_id;
    /**
     * 商品名称
     */
    private String goods_name;
    /**
     * 商品数量
     */
    private int quantity;
    /**
     * 商品单价
     */
    private int unit_price;

}
