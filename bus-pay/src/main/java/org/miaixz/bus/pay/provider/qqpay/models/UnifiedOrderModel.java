package org.miaixz.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 统一下单
 */
@Data
@Builder
@AllArgsConstructor
public class UnifiedOrderModel extends Property {

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
    private String time_start;
    private String time_expire;
    private String limit_pay;
    private String contract_code;
    private String promotion_tag;
    private String trade_type;
    private String notify_url;
    private String device_info;
    private String mini_app_param;

}
