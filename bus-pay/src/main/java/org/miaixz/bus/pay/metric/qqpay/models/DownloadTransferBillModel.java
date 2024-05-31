package org.miaixz.bus.pay.metric.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.pay.magic.Property;

/**
 * 企业付款对账单下载
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class DownloadTransferBillModel extends Property {

    private String mch_id;
    private String nonce_str;
    private String bill_date;
    private String sign;

}
