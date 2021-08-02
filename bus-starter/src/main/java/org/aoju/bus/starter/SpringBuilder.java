/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
 *                                                                               *
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
import org.aoju.bus.core.lang.Types;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * 设置相关系统参数信息.
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
@Component
public class SpringBuilder {

    /**
     * "@PostConstruct"注解标记的类中，由于ApplicationContext还未加载，导致空指针
     * 因此实现BeanFactoryPostProcessor注入ConfigurableApplicationContext实现bean的操作
     */
    private static ConfigurableApplicationContext context;

    public static ConfigurableApplicationContext getContext() {
        return SpringBuilder.context;
    }

    public static void setContext(ConfigurableApplicationContext context) {
        Assert.notNull(context, "Could not found context for spring.");
        SpringBuilder.context = context;
        SpringHolder.alive = true;
    }

    /**
     * 获取{@link ListableBeanFactory}，可能为{@link ConfigurableListableBeanFactory} 或 {@link ApplicationContextAware}
     *
     * @return {@link ListableBeanFactory}
     */
    public static ListableBeanFactory getBeanFactory() {
        return null == context.getBeanFactory() ? context : context.getBeanFactory();
    }

    /**
     * 通过name获取 Bean
     *
     * @param <T>  Bean类型
     * @param name Bean名称
     * @return Bean
     */
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param <T>   Bean类型
     * @param clazz Bean类
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return getBeanFactory().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param <T>   bean类型
     * @param name  Bean名称
     * @param clazz bean类型
     * @return Bean对象
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    /**
     * 通过类型参考返回带泛型参数的Bean
     *
     * @param reference 类型参考，用于持有转换后的泛型类型
     * @param <T>       Bean类型
     * @return 带泛型参数的Bean
     */
    public static <T> T getBean(Types<T> reference) {
        final ParameterizedType parameterizedType = (ParameterizedType) reference.getType();
        final Class<T> rawType = (Class<T>) parameterizedType.getRawType();
        final Class<?>[] genericTypes = Arrays.stream(parameterizedType.getActualTypeArguments()).map(type -> (Class<?>) type).toArray(Class[]::new);
        final String[] beanNames = getBeanFactory().getBeanNamesForType(ResolvableType.forClassWithGenerics(rawType, genericTypes));
        return getBean(beanNames[0], rawType);
    }

    /**
     * 获取指定类型对应的所有Bean，包括子类
     *
     * @param <T>  Bean类型
     * @param type 类、接口，null表示获取所有bean
     * @return 类型对应的bean，key是bean注册的name，value是Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 获取指定类型对应的Bean名称，包括子类
     *
     * @param type 类、接口，null表示获取所有bean名称
     * @return bean名称
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项key
     * @return 属性值
     */
    public static String getProperty(String key) {
        if (null == context) {
            return null;
        }
        return context.getEnvironment().getProperty(key);
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     */
    public static String[] getActiveProfiles() {
        if (null == context) {
            return null;
        }
        return context.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取当前的环境配置，当有多个环境配置时，只获取第一个
     *
     * @return 当前的环境配置
     */
    public static String getActiveProfile() {
        final String[] activeProfiles = getActiveProfiles();
        return ArrayKit.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
    }

    /**
     * 动态向Spring注册Bean
     *
     * @param clazz 类型
     */
    public static void registerBeanDefinition(Class clazz) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) getBeanFactory();
        beanFactory.registerBeanDefinition(StringKit.lowerFirst(clazz.getSimpleName()),
                BeanDefinitionBuilder.rootBeanDefinition(clazz).getBeanDefinition());
    }

    /**
     * 动态向Spring注册Bean
     *
     * @param clazz 类型
     */
    public static void registerSingleton(Class clazz) {
        try {
            registerSingleton(clazz, clazz.newInstance());
        } catch (InstantiationException e) {
            Logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    /**
     * 动态向Spring注册Bean
     * 由{@link org.springframework.beans.factory.BeanFactory} 实现，通过工具开放API
     *
     * @param clazz 类型
     * @param bean  对象
     */
    public static void registerSingleton(Class clazz, Object bean) {
        final ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) getBeanFactory();
        factory.autowireBean(bean);
        factory.registerSingleton(StringKit.lowerFirst(clazz.getSimpleName()), bean);
    }

    /**
     * 注销bean
     * 将Spring中的bean注销，请谨慎使用
     *
     * @param beanName bean名称
     */
    public static void unRegisterSingleton(String beanName) {
        final ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) getBeanFactory();
        if (factory instanceof DefaultSingletonBeanRegistry) {
            DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) factory;
            registry.destroySingleton(beanName);
        } else {
            throw new InstrumentException("Can not unregister bean, the factory is not a DefaultSingletonBeanRegistry!");
        }
    }

    /**
     * 加载或刷新配置信息，可能来自基于java的配置、XML文件、属性文件、关系数据库模式或其他一些格式
     */
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
