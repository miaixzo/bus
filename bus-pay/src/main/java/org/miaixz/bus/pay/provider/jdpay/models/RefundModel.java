package org.miaixz.bus.pay.provider.jdpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RefundModel extends JdPayEntity {
    private String version;
    private String merchant;
    private String tradeNum;
    private String oTradeNum;
    private String amount;
    private String currency;
    private String tradeTime;
    private String notifyUrl;
    private String note;
    private String sign;
    private String device;
    private String termInfoId;
    private String cert;
}
