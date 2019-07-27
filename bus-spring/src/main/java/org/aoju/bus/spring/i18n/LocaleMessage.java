package org.aoju.bus.spring.i18n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 获取本地 i18n 资源
 * key = value
 *
 * @author aoju.org
 * @version 3.0.1
 * @Group 839128
 * @since JDK 1.8
 */
@Component
public class LocaleMessage {

    private final MessageSource messageSource;

    @Autowired
    public LocaleMessage(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return 返回获取到的结果
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, null);
    }

    /**
     * @param code           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return 返回获取到的结果
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        //这里使用比较方便的方法，不依赖request.
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

}
