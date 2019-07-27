package org.aoju.bus.spring.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * swagger配置项
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties(prefix = "request.swagger")
public class SwaggerProperties {

    private String basePackage;
    private String title;
    private String serviceUrl;
    private String description;
    private String contact;
    private String version;

}