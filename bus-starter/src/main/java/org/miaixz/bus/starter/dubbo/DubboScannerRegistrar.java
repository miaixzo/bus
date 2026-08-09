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
package org.miaixz.bus.starter.dubbo;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationPostProcessor;
import org.apache.dubbo.config.spring.context.DubboSpringInitializer;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.spring.annotation.PlaceholderBinder;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableDubbo;

/**
 * Registers Dubbo service scanning for both annotation-based and property-based Starter activation.
 *
 * @author Kimi Liu
 */
public class DubboScannerRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    /**
     * Spring environment used to bind property-based Dubbo scanning options.
     */
    private Environment environment;

    /**
     * Initializes the registrar before Spring supplies its environment callback.
     */
    public DubboScannerRegistrar() {
        // No initialization required.
    }

    /**
     * Initializes Dubbo infrastructure and registers its service annotation processor.
     *
     * @param importingClassMetadata metadata of the importing Dubbo configuration
     * @param registry               current Bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DubboSpringInitializer.initialize(registry);
        if (containsServiceProcessor(registry)) {
            return;
        }

        Set<String> basePackages = resolveBasePackages(registry);
        if (basePackages.isEmpty()) {
            throw new IllegalStateException("Dubbo requires at least one service scan base package");
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceAnnotationPostProcessor.class)
                .addConstructorArgValue(basePackages).setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        BeanDefinitionReaderUtils.registerWithGeneratedName(builder.getBeanDefinition(), registry);
    }

    /**
     * Resolves scan packages from {@link EnableDubbo} first and bound Starter properties second.
     *
     * @param registry current Bean definition registry
     * @return ordered, de-duplicated service scan packages
     */
    private Set<String> resolveBasePackages(BeanDefinitionRegistry registry) {
        Set<String> basePackages = new LinkedHashSet<>();
        String applicationBasePackage = null;
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (registry.getBeanDefinition(beanName) instanceof AnnotatedBeanDefinition definition) {
                AnnotationMetadata metadata = definition.getMetadata();
                if (metadata.hasAnnotation(EnableDubbo.class.getName())
                        || metadata.hasMetaAnnotation(EnableDubbo.class.getName())) {
                    AnnotationAttributes attributes = AnnotationAttributes
                            .fromMap(metadata.getAnnotationAttributes(EnableDubbo.class.getName(), false));
                    basePackages.addAll(Arrays.asList(attributes.getStringArray("basePackages")));
                    for (Class<?> type : attributes.getClassArray("basePackageClasses")) {
                        basePackages.add(ClassKit.getPackageName(type));
                    }
                    if (basePackages.isEmpty()) {
                        basePackages.add(ClassKit.getPackageName(metadata.getClassName()));
                    }
                    return basePackages;
                }
                if (metadata.hasAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication")
                        || metadata.hasMetaAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication")) {
                    applicationBasePackage = ClassKit.getPackageName(metadata.getClassName());
                }
            }
        }

        DubboProperties properties = PlaceholderBinder.bind(environment, DubboProperties.class, GeniusBuilder.DUBBO);
        if (properties != null) {
            basePackages.addAll(Arrays.asList(properties.getBasePackages()));
            for (Class<?> type : properties.getBasePackageClasses()) {
                basePackages.add(ClassKit.getPackageName(type));
            }
        }
        if (basePackages.isEmpty() && applicationBasePackage != null) {
            basePackages.add(applicationBasePackage);
        }
        return basePackages;
    }

    /**
     * Returns whether an application or another Dubbo integration already registered the service processor.
     *
     * @param registry current Bean definition registry
     * @return {@code true} when a Dubbo service annotation processor is already registered
     */
    private static boolean containsServiceProcessor(BeanDefinitionRegistry registry) {
        String processorName = ServiceAnnotationPostProcessor.class.getName();
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (processorName.equals(registry.getBeanDefinition(beanName).getBeanClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the Spring environment used for property binding.
     *
     * @param environment current Spring environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
