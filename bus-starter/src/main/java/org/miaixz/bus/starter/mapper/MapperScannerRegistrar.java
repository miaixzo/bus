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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.annotation.PlaceholderBinder;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableMapper;

/**
 * An {@link ImportBeanDefinitionRegistrar} that handles the registration of mapper interfaces.
 * <p>
 * This class is imported by {@link MapperConfiguration}. It configures and launches a {@link MapperClassPathScanner}
 * for both annotation-based and property-based feature activation.
 *
 * @author Kimi Liu
 */
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    /**
     * The Spring {@link ResourceLoader} for resource location.
     */
    private ResourceLoader resourceLoader;

    /**
     * The Spring {@link Environment} for access to properties.
     */
    private Environment environment;

    /**
     * Initializes the registrar before Spring supplies its resource loader and environment callbacks.
     */
    public MapperScannerRegistrar() {
        // No initialization required.
    }

    /**
     * Registers mapper interfaces using annotation attributes first and configuration properties second.
     *
     * <p>
     * It creates a {@link MapperClassPathScanner}, configures it with attributes from {@link EnableMapper}, gathers the
     * base packages, and performs the bean definition scan.
     * </p>
     *
     * @param annotationMetadata metadata of the importing mapper configuration
     * @param registry           current Bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationMetadata enableMetadata = findEnableMapperMetadata(registry);
        AnnotationAttributes annotationAttributes = enableMetadata == null ? null
                : AnnotationAttributes
                        .fromMap(enableMetadata.getAnnotationAttributes(EnableMapper.class.getName(), false));
        MapperClassPathScanner scanner = new MapperClassPathScanner(registry);

        // Set the resource loader if available (required in Spring 3.1+).
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        Class<? extends Annotation> annotationClass = Annotation.class;
        Class<?> markerInterface = Class.class;
        Class<? extends BeanNameGenerator> generatorClass = BeanNameGenerator.class;
        Class<? extends MapperFactoryBean> mapperFactoryBeanClass = MapperFactoryBean.class;
        String sqlSessionTemplateRef = Normal.EMPTY;
        String sqlSessionFactoryRef = Normal.EMPTY;
        List<String> basePackage = new ArrayList<>();

        // Annotation attributes take precedence over the corresponding configuration properties.
        if (annotationAttributes != null) {
            annotationClass = annotationAttributes.getClass("annotationClass");
            markerInterface = annotationAttributes.getClass("markerInterface");
            generatorClass = annotationAttributes.getClass("nameGenerator");
            mapperFactoryBeanClass = annotationAttributes.getClass("factoryBean");
            sqlSessionTemplateRef = annotationAttributes.getString("sqlSessionTemplateRef");
            sqlSessionFactoryRef = annotationAttributes.getString("sqlSessionFactoryRef");
            basePackage.addAll(Arrays.asList(annotationAttributes.getStringArray("value")));
            basePackage.addAll(Arrays.asList(annotationAttributes.getStringArray("basePackage")));
            for (Class<?> clazz : annotationAttributes.getClassArray("basePackageClasses")) {
                basePackage.add(ClassKit.getPackageName(clazz));
            }
        }

        if (!Annotation.class.equals(annotationClass)) {
            scanner.setAnnotationClass(annotationClass);
        }

        if (!Class.class.equals(markerInterface)) {
            scanner.setMarkerInterface(markerInterface);
        }

        if (!BeanNameGenerator.class.equals(generatorClass)) {
            scanner.setBeanNameGenerator(ReflectKit.newInstanceIfPossible(generatorClass));
        }

        scanner.setMapperFactoryBeanClass(mapperFactoryBeanClass);
        scanner.setSqlSessionTemplateBeanName(sqlSessionTemplateRef);
        scanner.setSqlSessionFactoryBeanName(sqlSessionFactoryRef);

        // If no base packages are specified in the annotation, check properties.
        if (CollKit.isEmpty(basePackage)) {
            // Bind properties from the environment to MapperProperties.
            MapperProperties properties = PlaceholderBinder
                    .bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
            if (properties != null && properties.getBasePackage() != null && properties.getBasePackage().length > 0) {
                basePackage.addAll(Arrays.asList(properties.getBasePackage()));
            }
        }

        // An annotation without an explicit scan package enables the feature but does not override a configured
        // bus.mapper.base-package. Fall back to the annotated application package only when neither source specifies
        // a package.
        if (CollKit.isEmpty(basePackage) && enableMetadata != null) {
            basePackage.add(ClassKit.getPackageName(enableMetadata.getClassName()));
        }

        // Property-free auto-configuration scans application packages and only accepts explicit @Mapper types.
        if (CollKit.isEmpty(basePackage) && registry instanceof ConfigurableListableBeanFactory beanFactory
                && AutoConfigurationPackages.has(beanFactory)) {
            basePackage.addAll(AutoConfigurationPackages.get(beanFactory));
            scanner.setAnnotationClass(Mapper.class);
            annotationClass = Mapper.class;
        }
        if (CollKit.isEmpty(basePackage)) {
            throw new IllegalStateException(
                    "Mapper scanning requires @EnableMapper on an application class, bus.mapper.base-package, "
                            + "or Spring Boot auto-configuration packages");
        }

        // Register default filters and perform the component scan.
        scanner.registerFilters();
        Logger.debug(
                true,
                "Starter",
                "Mapper scanner registration started: basePackageCount={}, annotationClass={}",
                basePackage.size(),
                annotationClass.getName());
        java.util.Set<org.springframework.beans.factory.config.BeanDefinitionHolder> beanDefinitions = scanner
                .doScan(ArrayKit.ofArray(basePackage, String.class));
        Logger.info(
                false,
                "Starter",
                "Mapper scanner registration finished: basePackageCount={}, mapperBeanCount={}",
                basePackage.size(),
                beanDefinitions.size());
    }

    /**
     * Finds the mapper enable annotation declared by an application source.
     *
     * @param registry current Bean definition registry
     * @return merged annotation attributes, or {@code null} when properties activated the feature
     */
    private static AnnotationMetadata findEnableMapperMetadata(BeanDefinitionRegistry registry) {
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (registry.getBeanDefinition(beanName) instanceof AnnotatedBeanDefinition definition) {
                AnnotationMetadata metadata = definition.getMetadata();
                if (metadata.hasAnnotation(EnableMapper.class.getName())
                        || metadata.hasMetaAnnotation(EnableMapper.class.getName())) {
                    return metadata;
                }
            }
        }
        return null;
    }

    /**
     * Sets the Spring {@link Environment}.
     *
     * @param environment The environment to set.
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Sets the Spring {@link ResourceLoader}.
     *
     * @param resourceLoader The resource loader to set.
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
