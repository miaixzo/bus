/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.builder.MapperMethodTypeResolver;

/**
 * MyBatis mapper AOT and bean-definition processors.
 * <p>
 * This class groups the Spring AOT-specific infrastructure that used to live inside {@link MapperConfiguration}.
 * Keeping the processors here lets {@code MapperConfiguration} focus on normal mapper auto-configuration while
 * preserving the same bean registration points.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MapperAotProcessors {

    /**
     * Prevents instantiation of this utility holder.
     */
    private MapperAotProcessors() {
        // Utility holder; do not instantiate.
    }

    /**
     * AOT processor that discovers {@link MapperFactoryBean} beans and registers runtime hints for native compilation.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class MyBatisBeanFactoryInitializationAotProcessor
            implements BeanFactoryInitializationAotProcessor, BeanRegistrationExcludeFilter {

        /**
         * Infrastructure classes that should be skipped during AOT bean registration analysis.
         */
        private final Set<Class<?>> excludeClasses = new HashSet<>();

        /**
         * Constructs the AOT processor and records infrastructure beans that should be excluded from AOT processing.
         */
        MyBatisBeanFactoryInitializationAotProcessor() {
            excludeClasses.add(MapperScannerConfigurer.class);
        }

        /**
         * Returns whether a registered bean should be excluded from AOT processing.
         *
         * @param registeredBean registered bean descriptor
         * @return {@code true} when the bean class is excluded
         */
        @Override
        public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
            return excludeClasses.contains(registeredBean.getBeanClass());
        }

        /**
         * Creates the AOT contribution that registers mapper interfaces, XML resources, SQL provider types, return
         * types, and parameter types for native-image execution.
         *
         * @param beanFactory bean factory available during AOT processing
         * @return AOT contribution, or {@code null} when no mapper factory beans are present
         */
        @Override
        public BeanFactoryInitializationAotContribution processAheadOfTime(
                ConfigurableListableBeanFactory beanFactory) {
            String[] beanNames = beanFactory.getBeanNamesForType(MapperFactoryBean.class);
            if (beanNames.length == 0) {
                return null;
            }
            return (context, code) -> {
                RuntimeHints hints = context.getRuntimeHints();
                for (String beanName : beanNames) {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName.substring(1));
                    PropertyValue mapperInterface = beanDefinition.getPropertyValues()
                            .getPropertyValue("mapperInterface");
                    if (mapperInterface != null && mapperInterface.getValue() != null) {
                        Class<?> mapperInterfaceType = resolveMapperInterface(mapperInterface.getValue());
                        if (mapperInterfaceType != null) {
                            registerReflectionTypeIfNecessary(mapperInterfaceType, hints);
                            hints.proxies().registerJdkProxy(mapperInterfaceType);
                            String registerPattern = mapperInterfaceType.getName().replace(Symbol.C_DOT, Symbol.C_SLASH)
                                    .replace("org/miaixz/", Normal.EMPTY).concat(FileType.TYPE_XML);
                            hints.resources().registerPattern(registerPattern);
                            registerMapperRelationships(mapperInterfaceType, hints);
                        }
                    }
                }
            };
        }

        /**
         * Resolves the mapper interface class stored on a mapper factory bean definition.
         *
         * @param mapperInterfaceValue mapper interface property value
         * @return mapper interface class, or {@code null} when it cannot be loaded
         */
        private Class<?> resolveMapperInterface(Object mapperInterfaceValue) {
            if (mapperInterfaceValue instanceof Class) {
                return (Class<?>) mapperInterfaceValue;
            }
            if (mapperInterfaceValue instanceof String) {
                try {
                    return Class.forName((String) mapperInterfaceValue);
                } catch (ClassNotFoundException e) {
                    Logger.debug(false, "Starter", "Failed to load mapper interface class: {}", mapperInterfaceValue);
                    Logger.warn(
                            false,
                            "Starter",
                            e,
                            "Mapper interface class loading failed: mapperInterface={}, exception={}",
                            mapperInterfaceValue,
                            e.getClass().getSimpleName());
                }
            }
            return null;
        }

        /**
         * Registers reflection hints for all types related to mapper methods.
         * <p>
         * The mapper interface itself is already registered by the caller. This method walks declared mapper methods
         * and registers SQL provider classes plus resolved return and parameter payload classes.
         *
         * @param mapperInterfaceType mapper interface class
         * @param hints               runtime hints to update
         */
        private void registerMapperRelationships(Class<?> mapperInterfaceType, RuntimeHints hints) {
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(mapperInterfaceType);
            for (Method method : methods) {
                if (method.getDeclaringClass() != Object.class) {
                    ReflectionUtils.makeAccessible(method);
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

                    Class<?> returnType = MapperMethodTypeResolver.resolveReturnClass(mapperInterfaceType, method);
                    registerReflectionTypeIfNecessary(returnType, hints);
                    MapperMethodTypeResolver.resolveParameterClasses(mapperInterfaceType, method)
                            .forEach(x -> registerReflectionTypeIfNecessary(x, hints));
                }
            }
        }

        /**
         * Registers SQL provider classes referenced by provider annotations on a mapper method.
         *
         * @param method                mapper method being inspected
         * @param hints                 runtime hints to update
         * @param annotationType        provider annotation type
         * @param providerTypeResolvers functions extracting provider classes from the annotation
         * @param <T>                   provider annotation type
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
         * Registers a type for reflection when it is an application type.
         * <p>
         * Primitive and {@code java.*} types are intentionally skipped because they do not require explicit runtime
         * reflection hints for mapper payload handling.
         *
         * @param type  type to inspect
         * @param hints runtime hints to update
         */
        private void registerReflectionTypeIfNecessary(Class<?> type, RuntimeHints hints) {
            if (!type.isPrimitive() && !type.getName().startsWith("java")) {
                hints.reflection().registerType(type, MemberCategory.values());
            }
        }

    }

    /**
     * Post-processor that preserves the mapper factory bean post-processing registration point.
     * <p>
     * The actual String-to-Class conversion and target generic refresh are handled by
     * {@link MapperInterfaceStringToClassConverter}.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class MyBatisMapperFactoryBeanPostProcessor implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {

        /**
         * Accepts the configurable bean factory supplied by Spring.
         * <p>
         * The registration point is retained for compatibility with the original MyBatis mapper startup flow. The
         * current implementation performs mapper interface conversion in {@link MapperInterfaceStringToClassConverter}.
         *
         * @param beanFactory bean factory supplied by Spring
         */
        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            // Compatibility hook; mapper interface conversion happens in MapperInterfaceStringToClassConverter.
        }

        /**
         * Hook invoked after mapper factory bean definitions are merged.
         * <p>
         * The current implementation intentionally does not mutate the definition. It remains registered so downstream
         * behavior keeps the same extension point shape while conversion is handled by
         * {@link MapperInterfaceStringToClassConverter}.
         *
         * @param beanDefinition merged bean definition
         * @param beanType       resolved bean type
         * @param beanName       bean name
         */
        @Override
        public void postProcessMergedBeanDefinition(
                RootBeanDefinition beanDefinition,
                Class<?> beanType,
                String beanName) {
        }

    }

    /**
     * Converts String-based mapperInterface properties to Class objects in MapperFactoryBean definitions.
     * <p>
     * This adapter is necessary because AOT-generated bean definitions can store {@code mapperInterface} as a string.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class MapperInterfaceStringToClassConverter
            implements org.springframework.beans.factory.config.BeanFactoryPostProcessor {

        /**
         * Converts mapper factory bean definitions whose {@code mapperInterface} value is a class name string into a
         * {@link Class} instance.
         * <p>
         * This is required for AOT-generated definitions and also refreshes the target generic type so downstream
         * infrastructure can see {@code MapperFactoryBean<MapperInterface>}.
         *
         * @param beanFactory bean factory containing mapper factory bean definitions
         */
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            String[] allBeanNames = beanFactory.getBeanDefinitionNames();
            int processedCount = 0;

            Logger.debug(
                    true,
                    "Starter",
                    "Mapper interface conversion started: beanDefinitionCount={}",
                    allBeanNames.length);

            for (String beanName : allBeanNames) {
                try {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

                    if (beanDefinition instanceof RootBeanDefinition) {
                        RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;

                        if (rootBeanDefinition.hasBeanClass()
                                && MapperFactoryBean.class.isAssignableFrom(rootBeanDefinition.getBeanClass())) {

                            Object mapperInterfaceValue = rootBeanDefinition.getPropertyValues().get("mapperInterface");

                            Logger.debug(
                                    false,
                                    "Starter",
                                    "Mapper factory bean discovered: beanName={}, mapperInterfaceType={}",
                                    beanName,
                                    mapperInterfaceValue == null ? null : mapperInterfaceValue.getClass().getName());

                            if (mapperInterfaceValue instanceof String) {
                                String mapperInterfaceClassName = (String) mapperInterfaceValue;
                                Logger.debug(
                                        true,
                                        "Starter",
                                        "Mapper interface class conversion started: className={}",
                                        mapperInterfaceClassName);
                                try {
                                    Class<?> mapperInterface = ClassUtils
                                            .forName(mapperInterfaceClassName, beanFactory.getBeanClassLoader());

                                    rootBeanDefinition.getPropertyValues().removePropertyValue("mapperInterface");
                                    rootBeanDefinition.getPropertyValues()
                                            .addPropertyValue("mapperInterface", mapperInterface);

                                    rootBeanDefinition.setTargetType(
                                            ResolvableType
                                                    .forClassWithGenerics(MapperFactoryBean.class, mapperInterface));

                                    Logger.debug(
                                            false,
                                            "Starter",
                                            "Mapper interface class conversion completed: beanName={}, className={}",
                                            beanName,
                                            mapperInterface.getName());
                                    processedCount++;
                                } catch (ClassNotFoundException e) {
                                    Logger.error(
                                            false,
                                            "Starter",
                                            e,
                                            "Mapper interface class conversion failed: className={}, exception={}",
                                            mapperInterfaceClassName,
                                            e.getClass().getSimpleName());
                                }
                            } else if (mapperInterfaceValue instanceof Class) {
                                Logger.debug(
                                        false,
                                        "Starter",
                                        "Mapper interface conversion skipped: beanName={}, reason=alreadyClass",
                                        beanName);
                            } else {
                                Logger.debug(
                                        false,
                                        "Starter",
                                        "Mapper interface conversion skipped: beanName={}, mapperInterfaceType={}",
                                        beanName,
                                        mapperInterfaceValue == null ? null : mapperInterfaceValue.getClass());
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.warn(
                            false,
                            "Starter",
                            e,
                            "Mapper interface conversion failed for bean definition: beanName={}, exception={}",
                            beanName,
                            e.getClass().getSimpleName());
                }
            }
            Logger.debug(
                    false,
                    "Starter",
                    "Mapper interface conversion finished: processedMapperFactoryBeanCount={}",
                    processedCount);
        }

    }

}
