package org.aoju.bus.notify.provider.netease;

import lombok.Getter;
import org.aoju.bus.notify.metric.Properties;

/**
 * 云信配置
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
public class NeteaseProperties extends Properties {

    private String appNonce;

    public NeteaseProperties(String appNonce) {
        this.appNonce = appNonce;
    }
}
