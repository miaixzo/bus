/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;

/**
 * Spring上下文管理和Bean操作工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class SpringBuilder implements ApplicationContextAware {

    /**
     * "@PostConstruct"注解标记的类中，由于ApplicationContext还未加载，导致空指针
     * 因此实现BeanFactoryPostProcessor注入ConfigurableApplicationContext实现bean的操作
     */
    private static ConfigurableApplicationContext context;

    /**
     * 获取Spring上下文
     *
     * @return 上下文对象
     */
    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    /**
     * 设置Spring上下文
     *
     * @param context 上下文对象
     */
    public static void setContext(ConfigurableApplicationContext context) {
        Assert.notNull(context, "Spring context not found.");
        SpringBuilder.context = context;
        SpringHolder.alive = true;
    }

    /**
     * 获取Bean工厂
     *
     * @return Bean工厂对象
     */
    public static ListableBeanFactory getBeanFactory() {
        return context != null ? context.getBeanFactory() : null;
    }

    /**
     * 通过名称获取Bean
     *
     * @param name Bean名称
     * @param <T>  Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过类型获取Bean
     *
     * @param clazz Bean类型
     * @param args  构造函数参数
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz, Object... args) {
        return ArrayKit.isEmpty(args) ? getBeanFactory().getBean(clazz) : getBeanFactory().getBean(clazz, args);
    }

    /**
     * 通过名称和参数获取Bean
     *
     * @param name Bean名称
     * @param args 构造函数参数
     * @param <T>  Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Object... args) {
        return (T) (ArrayKit.isEmpty(args) ? getBeanFactory().getBean(name) : getBeanFactory().getBean(name, args));
    }

    /**
     * 通过名称和类型获取Bean
     *
     * @param name  Bean名称
     * @param clazz Bean类型
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    /**
     * 通过类型参考获取带泛型的Bean
     *
     * @param reference 类型参考
     * @param <T>       Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(TypeReference<T> reference) {
        ParameterizedType type = (ParameterizedType) reference.getType();
        Class<T> rawType = (Class<T>) type.getRawType();
        Class<?>[] genericTypes = Arrays.stream(type.getActualTypeArguments()).map(t -> (Class<?>) t)
                .toArray(Class[]::new);
        String[] beanNames = getBeanFactory()
                .getBeanNamesForType(ResolvableType.forClassWithGenerics(rawType, genericTypes));
        return getBean(beanNames[0], rawType);
    }

    /**
     * 获取指定类型的所有Bean
     *
     * @param type Bean类型
     * @param <T>  Bean类型
     * @return Bean名称到实例的映射
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 获取指定类型的Bean名称
     *
     * @param type Bean类型
     * @return Bean名称数组
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * 获取配置属性值
     *
     * @param key 属性键
     * @return 属性值
     */
    public static String getProperty(String key) {
        return context != null ? context.getEnvironment().getProperty(key) : null;
    }

    /**
     * 获取当前激活的环境配置
     *
     * @return 环境配置数组
     */
    public static String[] getActiveProfiles() {
        return context != null ? context.getEnvironment().getActiveProfiles() : null;
    }

    /**
     * 获取第一个激活的环境配置
     *
     * @return 环境配置 fcn
     */
    public static String getActiveProfile() {
        String[] profiles = getActiveProfiles();
        return ArrayKit.isNotEmpty(profiles) ? profiles[0] : null;
    }

    /**
     * 动态注册Bean定义
     *
     * @param clazz Bean类型
     */
    public static void registerBeanDefinition(Class<?> clazz) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) getBeanFactory();
        factory.registerBeanDefinition(StringKit.lowerFirst(clazz.getSimpleName()),
                BeanDefinitionBuilder.rootBeanDefinition(clazz).getBeanDefinition());
    }

    /**
     * 动态注册单例Bean
     *
     * @param clazz Bean类型
     */
    public static void registerSingleton(Class<?> clazz) {
        try {
            registerSingleton(clazz, clazz.getConstructor().newInstance());
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException
                | InstantiationException e) {
            Logger.error("Failed to register singleton for class: {}", clazz.getName(), e);
        }
    }

    /**
     * 动态注册单例Bean
     *
     * @param clazz Bean类型
     * @param bean  Bean实例
     */
    public static void registerSingleton(Class<?> clazz, Object bean) {
        ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) getBeanFactory();
        factory.autowireBean(bean);
        factory.registerSingleton(StringKit.lowerFirst(clazz.getSimpleName()), bean);
    }

    /**
     * 注销单例Bean
     *
     * @param beanName Bean名称
     */
    public static void unRegisterSingleton(String beanName) {
        ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) getBeanFactory();
        if (factory instanceof DefaultSingletonBeanRegistry registry) {
            registry.destroySingleton(beanName);
        } else {
            throw new InternalException("Cannot unregister bean: Factory is not DefaultSingletonBeanRegistry.");
        }
    }

    /**
     * 发布事件
     *
     * @param event 事件对象
     */
    public static void publishEvent(Object event) {
        if (context != null) {
            context.publishEvent(event);
        }
    }

    /**
     * 刷新Spring上下文
     */
    public static void refreshContext() {
        if (SpringHolder.alive) {
            context.refresh();
        }
    }

    /**
     * 关闭并移除Spring上下文
     */
    public static void removeContext() {
        if (SpringHolder.alive) {
            context.close();
            context = null;
            SpringHolder.alive = false;
        }
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称
     */
    public static String getApplicationName() {
        return getProperty(GeniusBuilder.APP_NAME);
    }

    /**
     * 判断是否为开发或测试模式
     *
     * @return 是否为开发/测试模式
     */
    public static boolean isDemoMode() {
        return isDevMode() || isTestMode();
    }

    /**
     * 判断是否为开发环境
     *
     * @return 是否为开发环境
     */
    public static boolean isDevMode() {
        return "dev".equalsIgnoreCase(getActiveProfile());
    }

    /**
     * 判断是否为测试环境
     *
     * @return 是否为测试环境
     */
    public static boolean isTestMode() {
        return "test".equalsIgnoreCase(getActiveProfile());
    }

    /**
     * 替换文本中的环境变量占位符
     *
     * @param text 待处理文本
     * @param env  环境配置
     * @return 替换后的文本
     */
    public static String replacePlaceholders(String text, ConfigurableEnvironment env) {
        if (context != null) {
            env = context.getEnvironment();
        }
        Properties props = new Properties();
        env.getPropertySources().forEach(source -> {
            if (source instanceof EnumerablePropertySource eps) {
                for (String name : eps.getPropertyNames()) {
                    props.put(name, String.valueOf(eps.getProperty(name)));
                }
            }
        });
        String result = text;
        for (String key : props.stringPropertyNames()) {
            result = result.replace(Symbol.DOLLAR + Symbol.BRACE_LEFT + key + Symbol.BRACE_RIGHT,
                    props.getProperty(key));
        }
        return result;
    }

    /**
     * 从BeanDefinition解析Bean的类类型
     *
     * @param beanDefinition Bean定义，非空
     * @return Bean的类类型，可能为null
     * @throws IllegalArgumentException 如果beanDefinition为null
     */
    public static Class<?> resolveBeanClassType(BeanDefinition beanDefinition) {
        if (beanDefinition == null) {
            throw new IllegalArgumentException("BeanDefinition cannot be null");
        }

        Class<?> clazz = null;
        String className = null;

        // 处理AnnotatedBeanDefinition类型
        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
            if (isFromConfigurationSource(beanDefinition)) {
                // 从工厂方法元数据获取返回类型
                MethodMetadata methodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
                className = methodMetadata != null ? methodMetadata.getReturnTypeName() : null;
            } else {
                // 从注解元数据获取类名
                AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                className = annotationMetadata != null ? annotationMetadata.getClassName() : null;
            }
        }

        // 尝试加载类
        if (StringKit.hasText(className)) {
            try {
                clazz = ClassKit.forName(className, null);
            } catch (Throwable e) {
                Logger.debug("Failed to load class: {}", className, e);
            }
        }

        // 如果类未解析，尝试从AbstractBeanDefinition获取
        if (clazz == null && beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
            try {
                clazz = abstractBeanDefinition.getBeanClass();
            } catch (IllegalStateException e) {
                Logger.debug("Failed to get bean class from AbstractBeanDefinition", e);
                className = beanDefinition.getBeanClassName();
                if (StringKit.hasText(className)) {
                    try {
                        clazz = ClassKit.forName(className, null);
                    } catch (Throwable ex) {
                        Logger.debug("Failed to load class from bean class name: {}", className, ex);
                    }
                }
            }
        }

        // 如果类仍未解析，尝试从RootBeanDefinition获取目标类型
        if (clazz == null && beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
            clazz = rootBeanDefinition.getTargetType();
        }

        return clazz;
    }

    /**
     * 检查BeanDefinition是否来源于配置类（而非其他配置源，如XML或组件扫描）。
     * <p>
     * 该方法通过检查BeanDefinition的类全限定名是否以
     * {@code org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader} 开头，
     * 判断其是否由Spring的{@code @Configuration}类生成。通常用于区分注解配置与传统XML配置或组件扫描的Bean定义。
     *
     * @param beanDefinition 要检查的Bean定义对象，非空
     * @return 如果BeanDefinition来源于配置类，返回{@code true}；否则返回{@code false}
     * @throws IllegalArgumentException 如果beanDefinition为null
     */
    public static boolean isFromConfigurationSource(BeanDefinition beanDefinition) {
        if (beanDefinition == null) {
            throw new IllegalArgumentException("BeanDefinition cannot be null");
        }
        return beanDefinition.getClass().getCanonicalName()
                .startsWith("org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBuilder.context = (ConfigurableApplicationContext) applicationContext;
    }

}