package org.miaixz.bus.pay.metric.wxpay.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * V3 统一下单-结算信息
 */
@Getter
@Setter
@Accessors(chain = true)
public class SettleInfo {

    /**
     * 是否指定分账
     */
    private boolean profit_sharing;
    /**
     * 补差金额
     */
    private int subsidy_amount;

}
