package org.aoju.bus.spring.i18n;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;

/**
 * @author aoju.org
 * @version 3.0.1
 * @Group 839128
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {I18nProperties.class})
public class I18nConfiguration {

    private final I18nProperties properties;

    @Autowired
    public I18nConfiguration(I18nProperties properties) {
        this.properties = properties;
    }

    private ResourceBundleMessageSource getMessageSource() {
        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setDefaultEncoding(StringUtils.defaultString(properties.getDefaultEncoding(), Charset.DEFAULT_UTF_8));
        bundleMessageSource.setBasenames(properties.getBaseNames());
        return bundleMessageSource;
    }

    /**
     * 注入 Validator 验证 Bean
     */
    @Bean
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(getMessageSource());
        return validator;
    }

}
