package org.miaixz.bus.pay.metric.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.pay.magic.Property;

/**
 * 关闭订单
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CloseOrderModel extends Property {

    private String appid;
    private String sub_appid;
    private String mch_id;
    private String sub_mch_id;
    private String nonce_str;
    private String sign;
    private String out_trade_no;
    private String total_fee;

}
