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
package org.miaixz.bus.starter.aot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.miaixz.bus.logger.Logger;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Spring Native AOT (Ahead-of-Time) configuration for MyBatis.
 * <p>
 * This class registers the necessary {@link RuntimeHints} for MyBatis mappers to work correctly in a GraalVM native
 * image. It handles:
 * <ul>
 * <li>Registering mapper interfaces for JDK proxying</li>
 * <li>Registering mapper XML resource files</li>
 * <li>Registering domain types (return types, parameter types) for reflection</li>
 * <li>Registering SQL provider classes (e.g., {@link SelectProvider}) for reflection</li>
 * <li>Ensuring {@link MapperFactoryBean} bean definitions are correctly processed</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(RuntimeHintsRegistrar.class)
public class MapperNativeConfiguration {

    /**
     * Registers the {@link BeanFactoryInitializationAotProcessor} that scans for {@link MapperFactoryBean} definitions
     * and registers runtime hints.
     * <p>
     * This method is declared as static to ensure it can be invoked without requiring the containing configuration
     * class to be instantiated first. The bean is marked with ROLE_INFRASTRUCTURE to indicate it is part of the Spring
     * infrastructure and should not be subject to post-processing.
     *
     * @return the AOT processor bean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MyBatisBeanFactoryInitializationAotProcessor myBatisBeanFactoryInitializationAotProcessor() {
        return new MyBatisBeanFactoryInitializationAotProcessor();
    }

    /**
     * Registers a {@link MergedBeanDefinitionPostProcessor} to resolve the generic type of {@link MapperFactoryBean}
     * bean definitions. This helps the AOT processing by exposing the mapper interface type.
     * <p>
     * This bean is only registered during AOT processing and is excluded from regular JVM runtime to avoid
     * BeanPostProcessor ordering warnings. The bean is marked with ROLE_INFRASTRUCTURE to indicate it is part of the
     * Spring infrastructure.
     *
     * @return the post-processor bean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Conditional(AotProcessingCondition.class)
    static MyBatisMapperFactoryBeanPostProcessor myBatisMapperFactoryBeanPostProcessor() {
        return new MyBatisMapperFactoryBeanPostProcessor();
    }

    /**
     * Condition that is true only during AOT processing phase. This prevents the bean from being created during normal
     * JVM runtime.
     */
    static class AotProcessingCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            // Only register this bean during AOT processing
            // During normal runtime, we don't need this post-processor
            return false; // Always false for JVM mode, true would be set by AOT processor
        }
    }

    /**
     * A {@link BeanFactoryInitializationAotProcessor} that discovers {@link MapperFactoryBean} beans and registers the
     * necessary runtime hints for native compilation.
     * <p>
     * It also implements {@link BeanRegistrationExcludeFilter} to exclude {@link MapperScannerConfigurer} from AOT
     * processing, as it is a {@code BeanFactoryPostProcessor} that is not needed at runtime.
     */
    static class MyBatisBeanFactoryInitializationAotProcessor
            implements BeanFactoryInitializationAotProcessor, BeanRegistrationExcludeFilter {

        /**
         * Set of classes to exclude from AOT processing.
         */
        private final Set<Class<?>> excludeClasses = new HashSet<>();

        /**
         * Constructs a new instance and initializes the set of excluded classes. {@link MapperScannerConfigurer} is
         * excluded because it is a BeanFactoryPostProcessor that is not required at native runtime.
         */
        MyBatisBeanFactoryInitializationAotProcessor() {
            excludeClasses.add(MapperScannerConfigurer.class);
        }

        /**
         * Determines whether a bean registration should be excluded from AOT processing.
         *
         * @param registeredBean the bean to check
         * @return {@code true} if the bean's class is in the exclude set; {@code false} otherwise
         */
        @Override
        public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
            return excludeClasses.contains(registeredBean.getBeanClass());
        }

        /**
         * Processes the bean factory ahead-of-time, contributing runtime hints for all discovered MyBatis
         * {@link MapperFactoryBean}s.
         *
         * @param beanFactory the configurable listable bean factory
         * @return a contribution with runtime hints, or {@code null} if no mappers are found
         */
        @Override
        public BeanFactoryInitializationAotContribution processAheadOfTime(
                ConfigurableListableBeanFactory beanFactory) {
            // Find all beans of type MapperFactoryBean
            String[] beanNames = beanFactory.getBeanNamesForType(MapperFactoryBean.class);
            if (beanNames.length == 0) {
                return null; // No mappers found, nothing to process
            }
            return (context, code) -> {
                RuntimeHints hints = context.getRuntimeHints();
                for (String beanName : beanNames) {
                    // Bean name has a '&' prefix for FactoryBean; remove it to get the bean definition
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName.substring(1));
                    // Retrieve the 'mapperInterface' property value
                    PropertyValue mapperInterface = beanDefinition.getPropertyValues()
                            .getPropertyValue("mapperInterface");
                    if (mapperInterface != null && mapperInterface.getValue() != null) {
                        Class<?> mapperInterfaceType = (Class<?>) mapperInterface.getValue();
                        if (mapperInterfaceType != null) {
                            // Register reflection hints for this mapper
                            registerReflectionTypeIfNecessary(mapperInterfaceType, hints);
                            // Register for JDK proxy
                            hints.proxies().registerJdkProxy(mapperInterfaceType);
                            // Register the corresponding XML resource file
                            // The 'org/miaixz/' prefix is removed per the replacement logic
                            // (see mapper/package-info.java for details)
                            String registerPattern = mapperInterfaceType.getName().replace('.', '/')
                                    .replace("org/miaixz/", "").concat(".xml");
                            hints.resources().registerPattern(registerPattern);
                            // Register related types (parameters, return types, providers)
                            registerMapperRelationships(mapperInterfaceType, hints);
                        }
                    }
                }
            };
        }

        /**
         * Registers reflection hints for all types related to a mapper interface.
         * <p>
         * This includes:
         * <ul>
         * <li>SQL provider classes (e.g., {@link SelectProvider}, {@link InsertProvider})</li>
         * <li>Method parameter types</li>
         * <li>Method return types</li>
         * </ul>
         *
         * @param mapperInterfaceType the mapper interface class
         * @param hints               the runtime hints instance
         */
        private void registerMapperRelationships(Class<?> mapperInterfaceType, RuntimeHints hints) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(mapperInterfaceType);
            for (Method method : methods) {
                if (method.getDeclaringClass() != Object.class) {
                    ReflectionUtils.makeAccessible(method);
                    // Register SQL provider annotations
                    registerSqlProviderTypes(
                            method,
                            hints,
                            SelectProvider.class,
                            SelectProvider::value,
                            SelectProvider::type);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            InsertProvider.class,
                            InsertProvider::value,
                            InsertProvider::type);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            UpdateProvider.class,
                            UpdateProvider::value,
                            UpdateProvider::type);
                    registerSqlProviderTypes(
                            method,
                            hints,
                            DeleteProvider.class,
                            DeleteProvider::value,
                            DeleteProvider::type);

                    // Register return types for reflection
                    Class<?> returnType = MyBatisMapperTypes.resolveReturnClass(mapperInterfaceType, method);
                    registerReflectionTypeIfNecessary(returnType, hints);
                    // Register parameter types for reflection
                    MyBatisMapperTypes.resolveParameterClasses(mapperInterfaceType, method)
                            .forEach(x -> registerReflectionTypeIfNecessary(x, hints));
                }
            }
        }

        /**
         * Scans a method for a given SQL provider annotation (e.g., {@link SelectProvider}) and registers the provider
         * class(es) for reflection.
         *
         * @param method                the method to scan
         * @param hints                 the runtime hints instance
         * @param annotationType        the annotation class (e.g., {@code SelectProvider.class})
         * @param providerTypeResolvers functions to extract the provider class from the annotation
         * @param <T>                   the type of the annotation
         */
        @SafeVarargs
        private <T extends Annotation> void registerSqlProviderTypes(
                Method method,
                RuntimeHints hints,
                Class<T> annotationType,
                Function<T, Class<?>>... providerTypeResolvers) {
            for (T annotation : method.getAnnotationsByType(annotationType)) {
                for (Function<T, Class<?>> providerTypeResolver : providerTypeResolvers) {
                    registerReflectionTypeIfNecessary(providerTypeResolver.apply(annotation), hints);
                }
            }
        }

        /**
         * Registers a type for reflection with all member categories, skipping primitives and core Java classes.
         *
         * @param type  the class to register
         * @param hints the runtime hints instance
         */
        private void registerReflectionTypeIfNecessary(Class<?> type, RuntimeHints hints) {
            if (!type.isPrimitive() && !type.getName().startsWith("java")) {
                hints.reflection().registerType(type, MemberCategory.values());
            }
        }

    }

    /**
     * Utility class to resolve actual parameter and return types from mapper methods, accounting for generics using
     * MyBatis's {@link TypeParameterResolver}.
     */
    static class MyBatisMapperTypes {

        /**
         * Private constructor to prevent instantiation of this utility class.
         */
        private MyBatisMapperTypes() {
            // Utility class - no instantiation
        }

        /**
         * Resolves the actual return class of a mapper method, considering generics.
         *
         * @param mapperInterface the mapper interface
         * @param method          the method
         * @return the resolved return class (e.g., {@code User} for a method returning {@code List<User>})
         */
        static Class<?> resolveReturnClass(Class<?> mapperInterface, Method method) {
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            return typeToClass(resolvedReturnType, method.getReturnType());
        }

        /**
         * Resolves the actual parameter classes of a mapper method, considering generics.
         *
         * @param mapperInterface the mapper interface
         * @param method          the method
         * @return a set of resolved parameter classes
         */
        static Set<Class<?>> resolveParameterClasses(Class<?> mapperInterface, Method method) {
            return Stream.of(TypeParameterResolver.resolveParamTypes(method, mapperInterface))
                    .map(x -> typeToClass(x, x instanceof Class ? (Class<?>) x : Object.class))
                    .collect(Collectors.toSet());
        }

        /**
         * Recursively extracts a {@link Class} from a {@link Type}.
         * <p>
         * Handles {@link Class}, {@link ParameterizedType}, and arrays. For {@link Map} types, it attempts to extract
         * the value type (the second type argument).
         *
         * @param src      the source {@link Type}
         * @param fallback a fallback class if resolution fails
         * @return the resolved {@link Class}
         */
        private static Class<?> typeToClass(Type src, Class<?> fallback) {
            Class<?> result = null;
            if (src instanceof Class<?>) {
                if (((Class<?>) src).isArray()) {
                    result = ((Class<?>) src).getComponentType();
                } else {
                    result = (Class<?>) src;
                }
            } else if (src instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) src;
                // For Map types, get the value type (index 1); otherwise use the first type argument
                int index = (parameterizedType.getRawType() instanceof Class
                        && Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())
                        && parameterizedType.getActualTypeArguments().length > 1) ? 1 : 0;
                Type actualType = parameterizedType.getActualTypeArguments()[index];
                result = typeToClass(actualType, fallback);
            }
            if (result == null) {
                result = fallback;
            }
            return result;
        }

    }

    /**
     * A {@link MergedBeanDefinitionPostProcessor} that ensures the {@link MapperFactoryBean} bean definition includes
     * generic type information.
     * <p>
     * This processor sets the {@link RootBeanDefinition#setTargetType(ResolvableType)} to include the mapper interface
     * as a generic parameter. This is crucial for AOT processing to determine the mapper interface type without
     * requiring early bean initialization.
     */
    static class MyBatisMapperFactoryBeanPostProcessor implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {

        /**
         * The fully qualified class name of the MapperFactoryBean.
         */
        private static final String MAPPER_FACTORY_BEAN = "org.mybatis.spring.mapper.MapperFactoryBean";

        /**
         * The bean factory instance.
         */
        private ConfigurableBeanFactory beanFactory;

        /**
         * Sets the bean factory for this post-processor.
         *
         * @param beanFactory the bean factory to be used
         */
        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }

        /**
         * Post-processes a merged bean definition, resolving the {@link MapperFactoryBean} target type if necessary.
         *
         * @param beanDefinition the merged bean definition
         * @param beanType       the bean type
         * @param beanName       the bean name
         */
        @Override
        public void postProcessMergedBeanDefinition(
                RootBeanDefinition beanDefinition,
                Class<?> beanType,
                String beanName) {
            if (ClassUtils.isPresent(MAPPER_FACTORY_BEAN, this.beanFactory.getBeanClassLoader())) {
                resolveMapperFactoryBeanTypeIfNecessary(beanDefinition);
            }
        }

        /**
         * If the bean definition is for a {@link MapperFactoryBean} and has unresolvable generics, this method sets the
         * target type to {@code MapperFactoryBean<TheActualMapperInterface>} to make the generic information available
         * for AOT processing.
         *
         * @param beanDefinition the bean definition to process
         */
        private void resolveMapperFactoryBeanTypeIfNecessary(RootBeanDefinition beanDefinition) {
            if (!beanDefinition.hasBeanClass()
                    || !MapperFactoryBean.class.isAssignableFrom(beanDefinition.getBeanClass())) {
                return;
            }
            // If the bean definition has unresolvable generics (e.g., was registered manually)
            if (beanDefinition.getResolvableType().hasUnresolvableGenerics()) {
                Class<?> mapperInterface = getMapperInterface(beanDefinition);
                if (mapperInterface != null) {
                    // Expose generic type information to the context to prevent early initialization
                    // Sets the type to MapperFactoryBean<TheActualMapperInterface>
                    beanDefinition.setTargetType(
                            ResolvableType.forClassWithGenerics(beanDefinition.getBeanClass(), mapperInterface));
                }
            }
        }

        /**
         * Safely retrieves the {@code mapperInterface} property value from a bean definition.
         *
         * @param beanDefinition the bean definition
         * @return the mapper interface class, or {@code null} if not found or an error occurs
         */
        private Class<?> getMapperInterface(RootBeanDefinition beanDefinition) {
            try {
                // This property is set by MapperScannerConfigurer or manual @Bean definition
                return (Class<?>) beanDefinition.getPropertyValues().get("mapperInterface");
            } catch (Exception e) {
                Logger.debug("Failed to retrieve mapper interface type.", e);
                return null;
            }
        }

    }

}
