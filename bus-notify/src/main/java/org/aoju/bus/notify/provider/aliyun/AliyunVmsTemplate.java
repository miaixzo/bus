package org.aoju.bus.notify.provider.aliyun;

import lombok.Builder;
import lombok.Getter;
import org.aoju.bus.notify.metric.Template;

/**
 * 阿里云语音通知模版
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Builder
public class AliyunVmsTemplate extends Template {

    /**
     * 语音模版参数
     */
    private String ttsParam;

    /**
     * 语音模版id
     */
    private String ttsCode;

    /**
     * 播放次数
     */
    private String playTimes;


}
