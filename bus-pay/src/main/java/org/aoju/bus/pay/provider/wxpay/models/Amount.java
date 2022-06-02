package org.aoju.bus.pay.provider.wxpay.models;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * V3 统一下单-订单金额
 */
@Data
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
