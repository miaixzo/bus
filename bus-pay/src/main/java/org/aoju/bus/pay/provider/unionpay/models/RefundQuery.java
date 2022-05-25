package org.aoju.bus.pay.provider.unionpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 云闪付-查询退款
 */
@Data
@Builder
@AllArgsConstructor
public class RefundQuery extends Property {

    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String out_trade_no;
    private String transaction_id;
    private String out_refund_no;
    private String refund_id;
    private String nonce_str;
    private String sign;
    private String sign_agentno;
    private String groupno;

}
