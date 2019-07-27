package org.aoju.bus.spring.mapper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mybatis配置项
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties(prefix = "spring.mybatis")
public class MybatisProperties {

    private String basePackage;
    private String xmlLocation;
    private String typeAliasesPackage;
    private String returnPageInfo;
    private String params;
    private String autoDelimitKeywords;
    private String reasonable;
    private String supportMethodsArguments;

}