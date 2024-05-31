package org.miaixz.bus.pay.metric.qqpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.pay.magic.Property;

/**
 * 现金红包对账单下载
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class DownloadHbBillModel extends Property {

    private String sign;
    private String mch_id;
    private String date;

}
