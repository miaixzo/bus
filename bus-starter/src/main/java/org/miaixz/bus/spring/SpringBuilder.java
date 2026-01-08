/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;

/**
 * Utility class for Spring context management and Bean operations.
 * <p>
 * This class provides static methods to access the Spring {@link ApplicationContext}, retrieve beans by various
 * criteria, manage bean definitions, publish events, and perform environment-related operations like placeholder
 * replacement. It implements {@link ApplicationContextAware} to capture the application context.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class SpringBuilder implements ApplicationContextAware {

    /**
     * The Spring {@link ConfigurableApplicationContext} instance. This is set via
     * {@link #setApplicationContext(ApplicationContext)}.
     */
    private static ConfigurableApplicationContext context;

    /**
     * Retrieves the Spring {@link ConfigurableApplicationContext}.
     *
     * @return The application context object.
     */
    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    /**
     * Sets the Spring {@link ConfigurableApplicationContext}.
     * <p>
     * This method is typically called by the Spring framework during initialization. It also sets the
     * {@link SpringHolder#alive} flag to {@code true}.
     * </p>
     *
     * @param context The application context object.
     */
    public static void setContext(ConfigurableApplicationContext context) {
        Assert.notNull(context, "Spring context not found.");
        SpringBuilder.context = context;
        SpringHolder.alive = true;
    }

    /**
     * Retrieves the Spring {@link ListableBeanFactory}.
     *
     * @return The BeanFactory object, or {@code null} if the context is not set.
     */
    public static ListableBeanFactory getBeanFactory() {
        return context != null ? context.getBeanFactory() : null;
    }

    /**
     * Retrieves a bean by its name.
     *
     * @param name The name of the bean.
     * @param <T>  The type of the bean.
     * @return The bean instance.
     */
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * Retrieves a bean by its type, optionally with constructor arguments.
     *
     * @param clazz The type of the bean.
     * @param args  Constructor arguments for the bean.
     * @param <T>   The type of the bean.
     * @return The bean instance.
     */
    public static <T> T getBean(Class<T> clazz, Object... args) {
        return ArrayKit.isEmpty(args) ? getBeanFactory().getBean(clazz) : getBeanFactory().getBean(clazz, args);
    }

    /**
     * Retrieves a bean by its type, optionally with constructor arguments.
     *
     * @param name The name of the bean.
     * @param args Constructor arguments for the bean.
     * @param <T>  The type of the bean.
     * @return The bean instance.
     */
    public static <T> T getBean(String name, Object... args) {
        return (T) (ArrayKit.isEmpty(args) ? getBeanFactory().getBean(name) : getBeanFactory().getBean(name, args));
    }

    /**
     * Retrieves a bean by its name and type.
     *
     * @param name  The name of the bean.
     * @param clazz The type of the bean.
     * @param <T>   The type of the bean.
     * @return The bean instance.
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    /**
     * Retrieves a bean with generic type information using a {@link TypeReference}.
     *
     * @param reference The {@link TypeReference} providing generic type information.
     * @param <T>       The generic type of the bean.
     * @return The bean instance matching the generic type.
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
     * Retrieves all beans of a specified type.
     *
     * @param type The type of the beans.
     * @param <T>  The type of the beans.
     * @return A map where keys are bean names and values are bean instances of the specified type.
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * Retrieves the names of all beans of a specified type.
     *
     * @param type The type of the beans.
     * @return An array of bean names.
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * Retrieves a property value from the Spring {@link Environment}.
     *
     * @param key The property key.
     * @return The property value, or {@code null} if not found or context is not set.
     */
    public static String getProperty(String key) {
        return context != null ? context.getEnvironment().getProperty(key) : null;
    }

    /**
     * Retrieves the current active profiles from the Spring {@link Environment}.
     *
     * @return An array of active profile names, or {@code null} if context is not set.
     */
    public static String[] getActiveProfiles() {
        return context != null ? context.getEnvironment().getActiveProfiles() : null;
    }

    /**
     * Retrieves the first active profile from the Spring {@link Environment}.
     *
     * @return The name of the first active profile, or {@code null} if no profiles are active or context is not set.
     */
    public static String getActiveProfile() {
        String[] profiles = getActiveProfiles();
        return ArrayKit.isNotEmpty(profiles) ? profiles[0] : null;
    }

    /**
     * Dynamically registers a bean definition with the Spring container.
     *
     * @param clazz The class of the bean to register.
     */
    public static void registerBeanDefinition(Class<?> clazz) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) getBeanFactory();
        factory.registerBeanDefinition(
                StringKit.lowerFirst(clazz.getSimpleName()),
                BeanDefinitionBuilder.rootBeanDefinition(clazz).getBeanDefinition());
    }

    /**
     * Dynamically registers a singleton bean with the Spring container.
     * <p>
     * This method attempts to create a new instance of the class using its default constructor.
     * </p>
     *
     * @param clazz The class of the singleton bean to register.
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
     * Dynamically registers a singleton bean instance with the Spring container.
     *
     * @param clazz The class of the singleton bean.
     * @param bean  The instance of the singleton bean.
     */
    public static void registerSingleton(Class<?> clazz, Object bean) {
        ConfigurableListableBeanFactory factory = (ConfigurableListableBeanFactory) getBeanFactory();
        factory.autowireBean(bean);
        factory.registerSingleton(StringKit.lowerFirst(clazz.getSimpleName()), bean);
    }

    /**
     * Unregisters a singleton bean from the Spring container.
     *
     * @param beanName The name of the bean to unregister.
     * @throws InternalException if the BeanFactory is not a {@link DefaultSingletonBeanRegistry}.
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
     * Publishes an event to the Spring application context.
     *
     * @param event The event object to publish.
     */
    public static void publishEvent(Object event) {
        if (context != null) {
            context.publishEvent(event);
        }
    }

    /**
     * Refreshes the Spring application context.
     * <p>
     * This operation is only performed if the context is currently alive.
     * </p>
     */
    public static void refreshContext() {
        if (SpringHolder.alive) {
            context.refresh();
        }
    }

    /**
     * Closes and removes the Spring application context.
     * <p>
     * This operation is only performed if the context is currently alive.
     * </p>
     */
    public static void removeContext() {
        if (SpringHolder.alive) {
            context.close();
            context = null;
            SpringHolder.alive = false;
        }
    }

    /**
     * Retrieves the application name from the Spring environment.
     *
     * @return The application name, or {@code null} if not found.
     */
    public static String getApplicationName() {
        return getProperty(GeniusBuilder.APP_NAME);
    }

    /**
     * Checks if the application is running in development or test mode.
     *
     * @return {@code true} if in dev or test mode, {@code false} otherwise.
     */
    public static boolean isDemoMode() {
        return isDevMode() || isTestMode();
    }

    /**
     * Checks if the application is running in development environment mode.
     *
     * @return {@code true} if in dev mode, {@code false} otherwise.
     */
    public static boolean isDevMode() {
        return "dev".equalsIgnoreCase(getActiveProfile());
    }

    /**
     * Checks if the application is running in test environment mode.
     *
     * @return {@code true} if in test mode, {@code false} otherwise.
     */
    public static boolean isTestMode() {
        return "test".equalsIgnoreCase(getActiveProfile());
    }

    /**
     * Replaces environment variable placeholders in the given text.
     * <p>
     * This method iterates through all property sources in the environment and replaces placeholders like
     * {@code ${property.name}} with their corresponding values.
     * </p>
     *
     * @param text The text containing placeholders.
     * @param env  The {@link ConfigurableEnvironment} to use for placeholder resolution.
     * @return The text with placeholders resolved.
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
            result = result
                    .replace(Symbol.DOLLAR + Symbol.BRACE_LEFT + key + Symbol.BRACE_RIGHT, props.getProperty(key));
        }
        return result;
    }

    /**
     * Resolves the class type of a bean from its {@link BeanDefinition}.
     * <p>
     * This method attempts to determine the actual {@link Class} of a bean by inspecting its {@link BeanDefinition},
     * handling various types like {@link AnnotatedBeanDefinition} and
     * {@link org.springframework.beans.factory.support.AbstractBeanDefinition}.
     * </p>
     *
     * @param beanDefinition The {@link BeanDefinition} of the bean, must not be {@code null}.
     * @return The resolved {@link Class} of the bean, or {@code null} if it cannot be determined.
     * @throws IllegalArgumentException if {@code beanDefinition} is {@code null}.
     */
    public static Class<?> resolveBeanClassType(BeanDefinition beanDefinition) {
        if (beanDefinition == null) {
            throw new IllegalArgumentException("BeanDefinition cannot be null");
        }

        Class<?> clazz = null;
        String className = null;

        // Handle AnnotatedBeanDefinition types
        if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
            if (isFromConfigurationSource(beanDefinition)) {
                // Get return type from factory method metadata
                MethodMetadata methodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
                className = methodMetadata != null ? methodMetadata.getReturnTypeName() : null;
            } else {
                // Get class name from annotation metadata
                AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                className = annotationMetadata != null ? annotationMetadata.getClassName() : null;
            }
        }

        // Attempt to load the class
        if (StringKit.hasText(className)) {
            try {
                clazz = ClassKit.forName(className, null);
            } catch (Throwable e) {
                Logger.debug("Failed to load class: {}", className, e);
            }
        }

        // If class is still not resolved, try to get it from AbstractBeanDefinition
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

        // If class is still not resolved, try to get target type from RootBeanDefinition
        if (clazz == null && beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
            clazz = rootBeanDefinition.getTargetType();
        }

        return clazz;
    }

    /**
     * Checks if a {@link BeanDefinition} originates from a Spring {@code @Configuration} class.
     * <p>
     * This method determines if a {@link BeanDefinition} was generated by Spring's configuration class processing
     * (e.g., from a method annotated with {@code @Bean}) rather than from other sources like XML configuration or
     * component scanning.
     * </p>
     *
     * @param beanDefinition The {@link BeanDefinition} object to check, must not be {@code null}.
     * @return {@code true} if the {@link BeanDefinition} originates from a configuration class; {@code false}
     *         otherwise.
     * @throws IllegalArgumentException if {@code beanDefinition} is {@code null}.
     */
    public static boolean isFromConfigurationSource(BeanDefinition beanDefinition) {
        if (beanDefinition == null) {
            throw new IllegalArgumentException("BeanDefinition cannot be null");
        }
        return beanDefinition.getClass().getCanonicalName()
                .startsWith("org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader");
    }

    /**
     * Sets the {@link ApplicationContext} for this utility class.
     * <p>
     * This method is part of the {@link ApplicationContextAware} interface implementation.
     * </p>
     *
     * @param applicationContext The application context to be set.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringBuilder.context = (ConfigurableApplicationContext) applicationContext;
    }

}
