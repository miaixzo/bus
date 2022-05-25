package org.aoju.bus.pay.provider.wxpay.models;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * V3 微信申请退款-金额信息
 */
@Data
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