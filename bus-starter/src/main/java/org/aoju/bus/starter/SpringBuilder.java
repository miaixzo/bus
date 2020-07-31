/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.starter;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 设置相关系统参数信息.
 *
 * @author Kimi Liu
 * @version 6.0.2
 * @since JDK 1.8+
 */
@Component
public class SpringBuilder {

    private static ConfigurableApplicationContext context;

    public static ConfigurableApplicationContext getContext() {
        return SpringBuilder.context;
    }

    public static void setContext(ConfigurableApplicationContext context) {
        Assert.notNull(context, "Could not found context for spring.");
        SpringBuilder.context = context;
        SpringHolder.alive = true;
    }

    public static void refreshContext() {
        if (SpringHolder.alive) {
            SpringBuilder.context.refresh();
        }
    }

    /**
     * 删除context信息
     */
    public static void removeContext() {
        if (SpringHolder.alive) {
            SpringBuilder.context.close();
            SpringBuilder.context = null;
            SpringHolder.alive = false;
        }
    }

    public static void register(Class clazz) {
        ConfigurableApplicationContext context = getContext();
        if (context != null) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            String beanName = StringKit.lowerFirst(clazz.getSimpleName());
            beanFactory.registerBeanDefinition(beanName, BeanDefinitionBuilder.rootBeanDefinition(clazz).getBeanDefinition());
        }
    }

    public static void registerSingleton(Class clazz) {
        try {
            registerSingleton(clazz, clazz.newInstance());
        } catch (InstantiationException e) {
            Logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    public static void registerSingleton(Class clazz, Object bean) {
        ConfigurableApplicationContext context = getContext();
        if (context != null) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            String beanName = StringKit.lowerFirst(clazz.getSimpleName());
            beanFactory.registerSingleton(beanName, bean);
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        ConfigurableApplicationContext context = getContext();
        if (context != null) {
            return context.getBean(clazz);
        }
        return null;
    }

    class TimeZoneBuilder {

        @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
        private String pattern;

        @Bean
        public Jackson2ObjectMapperBuilderCustomizer customizer() {
            return builder -> {
                builder.locale(Locale.CHINA);
                builder.timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
                builder.simpleDateFormat(pattern);
                builder.modules(new JavaTimeModule());
            };
        }

        class JavaTimeModule extends SimpleModule {
            JavaTimeModule() {
                super(PackageVersion.VERSION);
                this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Fields.NORM_DATETIME_PATTERN)));
                this.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(Fields.NORM_DATE_PATTERN)));
                this.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(Fields.NORM_TIME_PATTERN)));
                this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(Fields.NORM_DATETIME_PATTERN)));
                this.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(Fields.NORM_DATE_PATTERN)));
                this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(Fields.NORM_TIME_PATTERN)));
            }
        }

    }

}
