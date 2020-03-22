package org.aoju.bus.notify.provider.aliyun;

import lombok.Getter;
import lombok.Setter;
import org.aoju.bus.notify.metric.Template;

/**
 * 阿里云短信模版
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Setter
public class AliyunSmsTemplate extends Template {

    /**
     * 模版参数
     */
    String templateParam;


    /**
     * 模版id
     */
    String tempCode;

    /**
     * 短信签名
     */
    String smsSign;

}
