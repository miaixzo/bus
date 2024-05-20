package org.miaixz.bus.pay.metric.unionpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.miaixz.bus.pay.magic.Property;

/**
 * 云闪付-下单对账单
 */
@Data
@Builder
@AllArgsConstructor
public class BillDownloadModel extends Property {

    private String service;
    private String version;
    private String charset;
    private String bill_date;
    private String bill_type;
    private String sign_type;
    private String mch_id;
    private String nonce_str;
    private String sign;
}
