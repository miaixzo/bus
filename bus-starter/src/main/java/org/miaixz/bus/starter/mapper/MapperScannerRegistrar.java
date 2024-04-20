package org.miaixz.bus.starter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.miaixz.bus.core.toolkit.*;
import org.miaixz.bus.spring.BusXConfig;
import org.miaixz.bus.spring.PlaceBinder;
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry, importBeanNameGenerator);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableMapper.class.getName()));
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        // Spring 3.1中需要这个检查
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        if (!Annotation.class.equals(annotationClass)) {
            scanner.setAnnotationClass(annotationClass);
        }

        Class<?> markerInterface = annoAttrs.getClass("markerInterface");
        if (!Class.class.equals(markerInterface)) {
            scanner.setMarkerInterface(markerInterface);
        }

        Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            scanner.setBeanNameGenerator(ReflectKit.newInstanceIfPossible(generatorClass));
        }

        Class<? extends MapperFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
        if (!MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
            scanner.setMapperFactoryBean(ReflectKit.newInstanceIfPossible(mapperFactoryBeanClass));
        }

        scanner.setSqlSessionTemplateBeanName(annoAttrs.getString("sqlSessionTemplateRef"));
        scanner.setSqlSessionFactoryBeanName(annoAttrs.getString("sqlSessionFactoryRef"));

        List<String> basePackages = new ArrayList<>();
        for (String pkg : annoAttrs.getStringArray("value")) {
            if (StringKit.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : annoAttrs.getStringArray("basePackages")) {
            if (StringKit.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : annoAttrs.getClassArray("basePackageClasses")) {
            basePackages.add(ClassKit.getPackageName(clazz));
        }

        if (CollKit.isEmpty(basePackages)) {
            MybatisProperties properties = PlaceBinder.bind(environment, MybatisProperties.class, BusXConfig.MYBATIS);
            if (properties != null && properties.getBasePackages() != null && properties.getBasePackages().length > 0) {
                basePackages.addAll(Arrays.asList(properties.getBasePackages()));
            } else {
                // 未设置任何package的前提下，扫描@Mapper注解
                scanner.setAnnotationClass(Mapper.class);
            }
        }

        // 优先级 mapperBuilderRef > properties > springboot
        String mapperBuilderRef = annoAttrs.getString("mapperBuilderRef");
        String[] properties = annoAttrs.getStringArray("properties");
        if (StringKit.hasText(mapperBuilderRef)) {
            scanner.setMapperBuilderBeanName(mapperBuilderRef);
        } else if (properties != null && properties.length > 0) {
            scanner.setMapperProperties(properties);
        } else {
            scanner.setMapperProperties(environment);
        }
        scanner.registerFilters();
        scanner.doScan(ArrayKit.toArray(basePackages));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
