package org.miaixz.bus.pay.metric.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.pay.magic.Property;

/**
 * 查询红包详情
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class GetHbInfoModel extends Property {

    private String send_type;
    private String nonce_str;
    private String mch_id;
    private String mch_billno;
    private String listid;
    private String sub_mch_id;
    private String sign;

}
