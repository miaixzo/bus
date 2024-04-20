package org.miaixz.bus.pay.provider.jdpay.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserRelationModel extends JdPayEntity {
    private String version;
    private String merchant;
    private String userId;
    private String sign;
}
