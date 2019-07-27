package org.aoju.bus.spring.cors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Core 跨域相关配置
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties("request.cors")
public class CorsProperties {

    /**
     * 允许方法路径
     */
    private String path = "/**";
    /**
     * 允许的域名
     */
    private String[] allowedOrigins = new String[]{"*"};

    /**
     * 允许的请求头
     */
    private String[] allowedHeaders = new String[]{"*"};
    /**
     * 允许的方法
     */
    private String[] allowedMethods = new String[]{"GET", "DELETE", "POST", "PUT", "OPTIONS"};

    /**
     * 响应头信息公开
     */
    private String[] exposedHeaders;
    /**
     * 是否允许用户发送、处理 cookie
     */
    private Boolean allowCredentials = true;
    /**
     * 预检请求的有效期，单位为秒。有效期内，不会重复发送预检请求
     */
    private Long maxAge = 1800L;

}
