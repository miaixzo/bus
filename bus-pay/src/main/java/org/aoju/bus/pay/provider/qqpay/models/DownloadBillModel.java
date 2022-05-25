package org.aoju.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 对账单下载
 */
@Data
@Builder
@AllArgsConstructor
public class DownloadBillModel extends Property {

    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String bill_date;
    private String bill_type;
    private String tar_type;

}
