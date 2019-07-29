package org.aoju.bus.spring.druid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Druid 配置项
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class DruidProperties {

    private String key;
    private String name;
    private String url;
    private String username;
    private String password;
    private String type;
    private String driverClassName;
    private String filters;
    private String maxActive;
    private String initialSize;
    private String maxWait;
    private String minIdle;
    private String timeBetweenEvictionRunsMillis;
    private String minEvictableIdleTimeMillis;
    private String validationQuery;
    private String testWhileIdle;
    private String testOnBorrow;
    private String testOnReturn;
    private String poolPreparedStatements;
    private String maxOpenPreparedStatements;
    private String privateKey;

    private List<DruidProperties> multi;

}