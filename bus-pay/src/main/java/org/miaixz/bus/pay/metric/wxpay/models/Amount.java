package org.miaixz.bus.pay.metric.wxpay.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * V3 统一下单-订单金额
 */
@Getter
@Setter
@Accessors(chain = true)
public class Amount {

    /**
     * 总金额
     */
    private int total;
    /**
     * 货币类型
     */
    private String currency;

}
