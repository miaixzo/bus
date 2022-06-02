package org.aoju.bus.pay.provider.jdpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JdRequestModel extends JdPayEntity {
    private String version;
    private String merchant;
    private String encrypt;
}
