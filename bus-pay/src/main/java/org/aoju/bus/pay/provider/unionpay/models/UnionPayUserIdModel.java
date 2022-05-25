package org.aoju.bus.pay.provider.unionpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 云闪付-银联 JS 支付获取 userId
 */
@Data
@Builder
@AllArgsConstructor
public class UnionPayUserIdModel extends Property {

    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String user_auth_code;
    private String app_up_identifier;
    private String sign_agentno;
    private String groupno;

}
