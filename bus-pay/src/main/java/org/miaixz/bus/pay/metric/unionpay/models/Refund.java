package org.miaixz.bus.pay.metric.unionpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 云闪付-退款
 */
@Data
@Builder
@AllArgsConstructor
public class Refund extends Property {

    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String out_trade_no;
    private String transaction_id;
    private String out_refund_no;
    private String total_fee;
    private String refund_fee;
    private String op_user_id;
    private String refund_channel;
    private String nonce_str;
    private String sign;
    private String sign_agentno;
    private String groupno;

}
