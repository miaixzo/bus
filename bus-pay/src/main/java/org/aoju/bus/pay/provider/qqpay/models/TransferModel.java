package org.aoju.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 企业付款到余额
 */
@Data
@Builder
@AllArgsConstructor
public class TransferModel extends Property {

    private String input_charset;
    private String appid;
    private String openid;
    private String uin;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String out_trade_no;
    private String fee_type;
    private String total_fee;
    private String memo;
    private String check_name;
    private String re_user_name;
    private String check_real_name;
    private String op_user_id;
    private String op_user_passwd;
    private String spbill_create_ip;
    private String notify_url;

}
