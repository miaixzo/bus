package org.miaixz.bus.pay.provider.unionpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 云闪付-统一下单
 */
@Data
@Builder
@AllArgsConstructor
public class UnifiedOrder extends Property {

    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String appid;
    private String is_raw;
    private String is_minipg;
    private String out_trade_no;
    private String device_info;
    private String op_shop_id;
    private String body;
    private String sub_openid;
    private String user_id;
    private String attach;
    private String sub_appid;
    private String total_fee;
    private String need_receipt;
    private String customer_ip;
    private String mch_create_ip;
    private String notify_url;
    private String time_start;
    private String time_expire;
    private String qr_code_timeout_express;
    private String op_user_id;
    private String goods_tag;
    private String product_id;
    private String nonce_str;
    private String buyer_logon_id;
    private String buyer_id;
    private String limit_credit_pay;
    private String sign;
    private String sign_agentno;
    private String groupno;

}
