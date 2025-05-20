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
import java.util.Arrays;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * 通过{@code basePackage}， {@code annotationClass}或{@code markerInterface}注册映射器的{@link ClassPathBeanDefinitionScanner}
 * 如果指定了{@code annotationClass}和/或{@code markerInterface}，则只会搜索指定的类型(搜索所有接口将被禁用)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private String sqlSessionTemplateBeanName;

    private String sqlSessionFactoryBeanName;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private String mapperBuilderBeanName;

    private MapperFactoryBean<?> mapperFactoryBean = new MapperFactoryBean<>();

    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * 配置父扫描程序以搜索正确的接口 搜索所有接口或者只搜索扩展了markerInterface或annotationClass标注的接口
     */
    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // 如果指定了，则使用给定的注释或标记接口
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        // 重写AssignableTypeFilter以忽略实际标记接口上的匹配
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            // 默认包括接受所有类的过滤器
            addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        }

        // 排除 package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            if (className.endsWith("package-info")) {
                return true;
            }
            return metadataReader.getAnnotationMetadata()
                    .hasAnnotation("org.miaixz.bus.mapper.annotation.RegisterMapper");
        });
    }

    /**
     * 调用将搜索和注册进行处理，将它们设置为mapperFactoryBean
     *
     * @param basePackage 扫描路径
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackage) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackage);

        if (beanDefinitions.isEmpty()) {
            Logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackage)
                    + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            if (Logger.isDebugEnabled()) {
                Logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName() + "' and '"
                        + definition.getBeanClassName() + "' mapperInterface");
            }

            // 映射器接口是bean的原始类，但是bean的实际类是MapperFactoryBean
            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // issue
                                                                                                              // #59
            definition.setBeanClass(this.mapperFactoryBean.getClass());
            // 设置通用 Mapper
            if (StringKit.hasText(this.mapperBuilderBeanName)) {
                definition.getPropertyValues().add("mapperBuilder",
                        new RuntimeBeanReference(this.mapperBuilderBeanName));
            }

            boolean explicitFactoryUsed = false;
            if (StringKit.hasText(this.sqlSessionFactoryBeanName)) {
                definition.getPropertyValues().add("sqlSessionFactory",
                        new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
                explicitFactoryUsed = true;
            } else if (this.sqlSessionFactory != null) {
                definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
                explicitFactoryUsed = true;
            }
            if (StringKit.hasText(this.sqlSessionTemplateBeanName)) {
                if (explicitFactoryUsed) {
                    Logger.warn(
                            "Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
                }
                definition.getPropertyValues().add("sqlSessionTemplate",
                        new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
                explicitFactoryUsed = true;
            } else if (this.sqlSessionTemplate != null) {
                if (explicitFactoryUsed) {
                    Logger.warn(
                            "Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
                }
                definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
                explicitFactoryUsed = true;
            }
            if (!explicitFactoryUsed) {
                if (Logger.isDebugEnabled()) {
                    Logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName()
                            + "'.");
                }
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            }
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            Logger.warn(
                    "Skipping MapperFactoryBean with name '" + beanName + "' and '" + beanDefinition.getBeanClassName()
                            + "' mapperInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMapperFactoryBean(MapperFactoryBean<?> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean != null ? mapperFactoryBean : new MapperFactoryBean<>();
    }

    public void setMapperBuilderBeanName(String mapperBuilderBeanName) {
        this.mapperBuilderBeanName = mapperBuilderBeanName;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateBeanName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateBeanName;
    }

}
