package org.aoju.bus.notify.provider.dingtalk;

import lombok.Getter;
import lombok.Setter;
import org.aoju.bus.notify.metric.Properties;

/**
 * 钉钉配置
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Setter
public class DingTalkProperties extends Properties {

    private String agentId;

    private String corpId;

    private String whiteList;

}
