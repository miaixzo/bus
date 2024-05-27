package org.miaixz.bus.pay.metric.wxpay.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * V3 微信申请退款-金额信息
 */
@Getter
@Setter
@Accessors(chain = true)
public class RefundAmount {

    /**
     * 总金额
     */
    private int total;
    /**
     * 货币类型
     */
    private String currency;
    /**
     * 退款金额
     */
    private int refund;

}