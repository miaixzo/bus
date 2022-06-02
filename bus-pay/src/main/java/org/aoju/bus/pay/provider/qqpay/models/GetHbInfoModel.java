package org.aoju.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 查询红包详情
 */
@Data
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
