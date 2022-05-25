package org.aoju.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 提交付款码支付
 */
@Data
@Builder
@AllArgsConstructor
public class MicroPayModel extends Property {

    private String appid;
    private String sub_appid;
    private String mch_id;
    private String sub_mch_id;
    private String nonce_str;
    private String sign;
    private String body;
    private String attach;
    private String out_trade_no;
    private String fee_type;
    private String total_fee;
    private String spbill_create_ip;
    private String limit_pay;
    private String promotion_tag;
    private String notify_url;
    private String device_info;
    private String auth_code;
    private String trade_type;

}
