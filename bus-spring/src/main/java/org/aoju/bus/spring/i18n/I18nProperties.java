package org.aoju.bus.spring.i18n;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 国际化资源配置属性
 *
 * @author aoju.org
 * @version 3.0.1
 * @Group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties("request.i18n")
public class I18nProperties {

    /**
     * 默认 UTF-8
     */
    private String defaultEncoding;
    private String[] baseNames;

}
