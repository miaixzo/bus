package org.aoju.bus.notify.provider.aliyun;

import lombok.Getter;
import org.aoju.bus.notify.metric.Properties;

/**
 * 阿里云语音配置
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
public class AliyunVmsProperties extends Properties {

    /**
     * 主叫号码
     */
    String showNumber;
}
