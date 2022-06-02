package org.aoju.bus.pay.provider.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.aoju.bus.pay.magic.Property;

/**
 * 现金红包对账单下载
 */
@Data
@Builder
@AllArgsConstructor
public class DownloadHbBillModel extends Property {

    private String sign;
    private String mch_id;
    private String date;

}
