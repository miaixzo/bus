package org.miaixz.bus.pay.metric.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 查询订单
 */
@Data
@Builder
@AllArgsConstructor
public class OrderQueryModel extends Property {

    private String appid;
    private String sub_appid;
    private String mch_id;
    private String sub_mch_id;
    private String nonce_str;
    private String sign;
    private String transaction_id;
    private String out_trade_no;

}
