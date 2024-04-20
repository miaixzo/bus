package org.miaixz.bus.pay.provider.jdpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class QueryOrderModel extends JdPayEntity {
    private String version;
    private String merchant;
    private String tradeNum;
    private String oTradeNum;
    private String tradeType;
    private String sign;
}
