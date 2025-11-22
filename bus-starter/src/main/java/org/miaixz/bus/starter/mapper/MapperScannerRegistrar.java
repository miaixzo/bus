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
package org.miaixz.bus.starter.mapper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;
import org.miaixz.bus.starter.annotation.EnableMapper;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

/**
 * An {@link ImportBeanDefinitionRegistrar} that handles the registration of mapper interfaces.
 * <p>
 * This class is triggered by the {@link EnableMapper} annotation. It configures and launches a
 * {@link ClassPathMapperScanner} to discover and register mapper interfaces as Spring beans.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * Default implementation of the {@code registerBeanDefinitions} method. This is part of the
     * {@link ImportBeanDefinitionRegistrar} interface.
     * <p>
     * This implementation simply delegates to the default method in the interface and is provided for compatibility.
     * </p>
     *
     * @param importingClassMetadata  The annotation metadata of the importing class.
     * @param registry                The bean definition registry.
     * @param importBeanNameGenerator The bean name generator.
     */
    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry,
            BeanNameGenerator importBeanNameGenerator) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(
                importingClassMetadata,
                registry,
                importBeanNameGenerator);
    }

    /**
     * Registers bean definitions for mapper interfaces based on the {@link EnableMapper} annotation metadata.
     *
     * <p>
     * It creates a {@link ClassPathMapperScanner}, configures it with attributes from {@link EnableMapper}, gathers the
     * base packages, and performs the bean definition scan.
     * </p>
     *
     * @param annotationMetadata The annotation metadata of the importing class, typically having the
     *                           {@link EnableMapper} annotation.
     * @param registry           The bean definition registry.
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(EnableMapper.class.getName()));
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

        // Set the resource loader if available (required in Spring 3.1+).
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        // Configure the scanner from the annotation attributes.

        // Sets the annotation that the scanner searches for.
        Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        if (!Annotation.class.equals(annotationClass)) {
            scanner.setAnnotationClass(annotationClass);
        }

        // Sets the marker interface that the scanner searches for.
        Class<?> markerInterface = annoAttrs.getClass("markerInterface");
        if (!Class.class.equals(markerInterface)) {
            scanner.setMarkerInterface(markerInterface);
        }

        // Sets the bean name generator.
        Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            scanner.setBeanNameGenerator(ReflectKit.newInstanceIfPossible(generatorClass));
        }

        // Sets the custom MapperFactoryBean class.
        Class<? extends MapperFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
        if (!MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
            scanner.setMapperFactoryBean(ReflectKit.newInstanceIfPossible(mapperFactoryBeanClass));
        }

        // Sets the bean names for SqlSessionTemplate and SqlSessionFactory.
        scanner.setSqlSessionTemplateBeanName(annoAttrs.getString("sqlSessionTemplateRef"));
        scanner.setSqlSessionFactoryBeanName(annoAttrs.getString("sqlSessionFactoryRef"));

        // Gather base packages to scan from the annotation's attributes.
        List<String> basePackage = new ArrayList<>();
        basePackage.addAll(Arrays.asList(annoAttrs.getStringArray("value")));
        basePackage.addAll(Arrays.asList(annoAttrs.getStringArray("basePackage")));
        for (Class<?> clazz : annoAttrs.getClassArray("basePackageClasses")) {
            basePackage.add(ClassKit.getPackageName(clazz));
        }

        // If no base packages are specified in the annotation, check properties.
        if (CollKit.isEmpty(basePackage)) {
            // Bind properties from the environment to MapperProperties.
            MapperProperties properties = PlaceHolderBinder
                    .bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
            if (properties != null && properties.getBasePackage() != null && properties.getBasePackage().length > 0) {
                basePackage.addAll(Arrays.asList(properties.getBasePackage()));
            } else {
                // If no packages are set via annotation or properties, scan for the @Mapper annotation by default.
                scanner.setAnnotationClass(Mapper.class);
            }
        }

        // Register default filters and perform the component scan.
        scanner.registerFilters();
        scanner.doScan(ArrayKit.ofArray(basePackage, String.class));
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
