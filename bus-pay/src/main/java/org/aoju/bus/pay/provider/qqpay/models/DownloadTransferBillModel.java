package org.aoju.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 企业付款对账单下载
 */
@Data
@Builder
@AllArgsConstructor
public class DownloadTransferBillModel extends Property {

    private String mch_id;
    private String nonce_str;
    private String bill_date;
    private String sign;

}
