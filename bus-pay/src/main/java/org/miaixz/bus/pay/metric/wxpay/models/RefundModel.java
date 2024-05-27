package org.miaixz.bus.pay.metric.wxpay.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * V3 微信申请退款
 */
@Getter
@Setter
@Accessors(chain = true)
public class RefundModel {

    /**
     * 微信支付订单号
     */
    private String transaction_id;
    /**
     * 商户订单号
     */
    private String out_trade_no;
    /**
     * 商户退款单号
     */
    private String out_refund_no;
    /**
     * 退款原因
     */
    private String reason;
    /**
     * 退款结果回调url
     */
    private String notify_url;
    /**
     * 退款资金来源
     */
    private String funds_account;
    /**
     * 金额信息
     */
    private RefundAmount amount;
    /**
     * 退款商品
     */
    private List<RefundGoodsDetail> goods_detail;

}


