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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.builder.MapperMethodTypeResolver;
import org.miaixz.bus.spring.annotation.PlaceholderBinder;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * MyBatis mapper AOT and bean-definition processors.
 * <p>
 * This class groups the Spring AOT-specific infrastructure that used to live inside {@link MapperConfiguration}.
 * Keeping the processors here lets {@code MapperConfiguration} focus on normal mapper Bean assembly while preserving
 * the same bean registration points.
 *
 * @author Kimi Liu
 */
public final class MapperAotProcessors {

    /**
     * Keeps mapper AOT processor discovery on the static API.
     */
    private MapperAotProcessors() {
        // No initialization required.
    }

    /**
     * AOT processor that discovers {@link MapperFactoryBean} beans and registers runtime hints for native compilation.
     *
     * @author Kimi Liu
     */
    public static class MyBatisBeanFactoryInitializationAotProcessor
            implements BeanFactoryInitializationAotProcessor, BeanRegistrationExcludeFilter {

        /**
         * Infrastructure classes that should be skipped during AOT bean registration analysis.
         */
        private final Set<Class<?>> excludeClasses = new HashSet<>();

        /**
         * Spring environment used to bind mapper locations before regular configuration property beans are available.
         */
        private final Environment environment;

        /**
         * Constructs the AOT processor and records infrastructure beans that should be excluded from AOT processing.
         *
         * @param environment Spring environment used for early mapper property binding
         */
        public MyBatisBeanFactoryInitializationAotProcessor(Environment environment) {
            this.environment = environment;
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
            MapperProperties properties = PlaceholderBinder
                    .bind(this.environment, MapperProperties.class, GeniusBuilder.MAPPER);
            if (properties == null) {
                properties = new MapperProperties();
            }
            List<String> basePackages = normalizeBasePackages(properties.getBasePackage());
            if (basePackages.isEmpty()) {
                return null;
            }
            String[] beanNames = beanFactory.getBeanNamesForType(MapperFactoryBean.class);
            if (beanNames.length == 0) {
                return null;
            }
            MapperLocationResolver.Result mapperLocations = MapperLocationResolver
                    .resolve(properties, new PathMatchingResourcePatternResolver(beanFactory.getBeanClassLoader()));
            Logger.info(
                    false,
                    "Starter",
                    "Mapper AOT resources resolved: resourceCount={}, patterns={}",
                    mapperLocations.resources().length,
                    mapperLocations.patterns());
            return (context, code) -> {
                RuntimeHints hints = context.getRuntimeHints();
                mapperLocations.patterns().forEach(hints.resources()::registerPattern);
                for (String beanName : beanNames) {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName.substring(1));
                    PropertyValue mapperInterface = beanDefinition.getPropertyValues()
                            .getPropertyValue("mapperInterface");
                    if (mapperInterface != null && mapperInterface.getValue() != null) {
                        Class<?> mapperInterfaceType = resolveMapperInterface(mapperInterface.getValue());
                        if (isConfiguredMapper(mapperInterfaceType, basePackages)) {
                            registerReflectionTypeIfNecessary(mapperInterfaceType, hints);
                            hints.proxies().registerJdkProxy(mapperInterfaceType);
                            registerMapperRelationships(mapperInterfaceType, hints);
                        }
                    }
                }
            };
        }

        /**
         * Normalizes the base packages.
         *
         * @param configuredPackages configured packages
         * @return normalized, de-duplicated mapper packages in declaration order
         */
        private static List<String> normalizeBasePackages(String[] configuredPackages) {
            if (configuredPackages == null || configuredPackages.length == 0) {
                return List.of();
            }
            return Arrays.stream(configuredPackages)
                    .filter(packageName -> packageName != null && !packageName.isBlank()).map(String::trim).distinct()
                    .toList();
        }

        /**
         * Tests whether the candidate type is a mapper explicitly included by Starter configuration.
         *
         * @param type         candidate mapper interface
         * @param basePackages base packages
         * @return whether configured mapper
         */
        private static boolean isConfiguredMapper(Class<?> type, List<String> basePackages) {
            if (type == null || !type.isInterface()) {
                return false;
            }
            String packageName = type.getPackageName();
            return basePackages.stream().anyMatch(
                    basePackage -> packageName.equals(basePackage) || packageName.startsWith(basePackage + Symbol.DOT));
        }

        /**
         * Resolves the mapper interface class stored on a mapper factory bean definition.
         *
         * @param mapperInterfaceValue mapper interface represented as a Class or fully qualified class name
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
         * The mapper interface itself is already registered by the caller. This method walks declared methods across
         * the complete mapper-interface hierarchy and registers SQL provider classes plus resolved return and parameter
         * payload classes.
         *
         * @param mapperInterfaceType mapper interface class
         * @param hints               runtime hints to update
         */
        private void registerMapperRelationships(Class<?> mapperInterfaceType, RuntimeHints hints) {
            Set<Class<?>> mapperTypes = new LinkedHashSet<>();
            mapperTypes.add(mapperInterfaceType);
            mapperTypes.addAll(ClassKit.getInterfaces(mapperInterfaceType));
            for (Class<?> mapperType : mapperTypes) {
                for (Method method : MethodKit.getDeclaredMethods(mapperType)) {
                    if (method.getDeclaringClass() != Object.class) {
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
                FunctionX<T, Class<?>>... providerTypeResolvers) {
            for (T annotation : method.getAnnotationsByType(annotationType)) {
                for (FunctionX<T, Class<?>> providerTypeResolver : providerTypeResolvers) {
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
                ReflectionHints reflection = hints.reflection();
                reflection.registerType(type);

                Set<Constructor<?>> constructors = new LinkedHashSet<>(Set.of(type.getDeclaredConstructors()));
                constructors.addAll(Set.of(type.getConstructors()));
                constructors.forEach(constructor -> reflection.registerConstructor(constructor, ExecutableMode.INVOKE));

                Set<Field> fields = new LinkedHashSet<>(Set.of(type.getDeclaredFields()));
                fields.addAll(Set.of(type.getFields()));
                fields.forEach(reflection::registerField);

                Set<Method> methods = new LinkedHashSet<>(Set.of(type.getDeclaredMethods()));
                methods.addAll(Set.of(type.getMethods()));
                methods.forEach(method -> reflection.registerMethod(method, ExecutableMode.INVOKE));
            }
        }

    }

    /**
     * Converts String-based mapperInterface properties to Class objects in MapperFactoryBean definitions.
     * <p>
     * This adapter is necessary because AOT-generated bean definitions can store {@code mapperInterface} as a string.
     *
     * @author Kimi Liu
     */
    public static class MapperInterfaceStringToClassConverter
            implements org.springframework.beans.factory.config.BeanFactoryPostProcessor {

        /**
         * Initializes the post-processor that resolves mapper interface class names before Bean creation.
         */
        MapperInterfaceStringToClassConverter() {
            // No initialization required.
        }

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
                                    Class<?> mapperInterface = ClassKit
                                            .forName(mapperInterfaceClassName, beanFactory.getBeanClassLoader());

                                    rootBeanDefinition.getPropertyValues().removePropertyValue("mapperInterface");
                                    rootBeanDefinition.getPropertyValues()
                                            .addPropertyValue("mapperInterface", mapperInterface);

                                    prepareMapperDefinition(rootBeanDefinition, mapperInterface);

                                    Logger.debug(
                                            false,
                                            "Starter",
                                            "Mapper interface class conversion completed: beanName={}, className={}",
                                            beanName,
                                            mapperInterface.getName());
                                    processedCount++;
                                } catch (InternalException e) {
                                    Logger.error(
                                            false,
                                            "Starter",
                                            e,
                                            "Mapper interface class conversion failed: className={}, exception={}",
                                            mapperInterfaceClassName,
                                            e.getClass().getSimpleName());
                                }
                            } else if (mapperInterfaceValue instanceof Class) {
                                Class<?> mapperInterface = (Class<?>) mapperInterfaceValue;
                                prepareMapperDefinition(rootBeanDefinition, mapperInterface);
                                Logger.debug(
                                        false,
                                        "Starter",
                                        "Mapper factory bean type metadata restored: beanName={}, className={}",
                                        beanName,
                                        mapperInterface.getName());
                                processedCount++;
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

        /**
         * Restores the mapper product type metadata required for dependency lookup.
         *
         * @param beanDefinition  mapper factory bean definition
         * @param mapperInterface mapper interface exposed by the factory bean
         */
        private void prepareMapperDefinition(RootBeanDefinition beanDefinition, Class<?> mapperInterface) {
            beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, mapperInterface);
            beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(MapperFactoryBean.class, mapperInterface));
        }

    }

}
