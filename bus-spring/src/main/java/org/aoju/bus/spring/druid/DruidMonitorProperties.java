package org.aoju.bus.spring.druid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Druid 监控配置项
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties(prefix = "spring.druid.monitor")
public class DruidMonitorProperties {

    private String DruidStatView;
    private String DruidWebStatFilter;

    private String allow;
    private String deny;
    private String loginUsername;
    private String loginPassword;

    private String exclusions;
    private String resetEnable;

}