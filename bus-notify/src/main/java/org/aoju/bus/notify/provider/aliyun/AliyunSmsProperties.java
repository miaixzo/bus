package org.aoju.bus.notify.provider.aliyun;

import lombok.Getter;
import org.aoju.bus.notify.metric.Properties;

/**
 * 阿里云短信配置
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
public class AliyunSmsProperties extends Properties {

    /**
     * 短信签名
     */
    private String signName;


}
