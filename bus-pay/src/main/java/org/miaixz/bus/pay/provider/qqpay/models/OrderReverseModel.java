package org.miaixz.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 撤销订单
 */
@Data
@Builder
@AllArgsConstructor
public class OrderReverseModel extends Property {
    private String appid;
    private String sub_appid;
    private String mch_id;
    private String sub_mch_id;
    private String nonce_str;
    private String sign;
    private String out_trade_no;
    private String op_user_id;
    private String op_user_passwd;
}
