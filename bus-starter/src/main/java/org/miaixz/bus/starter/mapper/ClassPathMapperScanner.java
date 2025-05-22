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
 * MyBatis映射器扫描器，通过包路径、注解或标记接口注册映射器 支持指定注解或接口过滤，禁用搜索所有接口时仅扫描指定类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

    /**
     * MyBatis SqlSession工厂，用于创建SqlSession
     */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * MyBatis SqlSession模板，提供线程安全的SqlSession操作
     */
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * SqlSessionTemplate的Bean名称，用于Spring容器引用
     */
    private String sqlSessionTemplateBeanName;

    /**
     * SqlSessionFactory的Bean名称，用于Spring容器引用
     */
    private String sqlSessionFactoryBeanName;

    /**
     * 扫描的注解类型，用于过滤特定注解的接口
     */
    private Class<? extends Annotation> annotationClass;

    /**
     * 扫描的标记接口，用于过滤实现特定接口的类
     */
    private Class<?> markerInterface;

    /**
     * MapperBuilder的Bean名称，用于配置通用Mapper
     */
    private String mapperBuilderBeanName;

    /**
     * Mapper工厂Bean，用于创建Mapper实例
     */
    private MapperFactoryBean<?> mapperFactoryBean = new MapperFactoryBean<>();

    /**
     * 构造函数，初始化映射器扫描器
     *
     * @param registry Spring Bean定义注册器
     */
    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * 配置扫描过滤器，设置搜索的接口或注解规则 支持注解、标记接口或默认扫描所有接口，排除package-info.java
     */
    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // 添加注解过滤器
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        // 添加接口过滤器，忽略标记接口本身
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        // 默认包含所有接口
        if (acceptAllInterfaces) {
            addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        }

        // 排除 package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> metadataReader.getClassMetadata().getClassName()
                .endsWith("package-info"));
    }

    /**
     * 扫描指定包路径，注册映射器Bean 如果未找到映射器，记录警告日志；否则处理Bean定义
     *
     * @param basePackages 扫描的基础包路径
     * @return 注册的Bean定义集合
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            Logger.warn("No MyBatis mapper was found in '{}' package. Please check your configuration.",
                    Arrays.toString(basePackages));
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    /**
     * 处理扫描到的Bean定义，配置MapperFactoryBean 设置映射器接口、MapperFactoryBean、SqlSession和MapperBuilder
     *
     * @param beanDefinitions 扫描到的Bean定义集合
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            if (Logger.isDebugEnabled()) {
                Logger.debug("Creating MapperFactoryBean with name '{}' and '{}' mapperInterface", holder.getBeanName(),
                        definition.getBeanClassName());
            }

            // 映射器接口是bean的原始类，但是bean的实际类是MapperFactoryBean
            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());
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
                    Logger.debug("Enabling autowire by type for MapperFactoryBean with name '{}'",
                            holder.getBeanName());
                }
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            }
        }
    }

    /**
     * 判断是否为候选组件，仅限接口且独立
     *
     * @param beanDefinition Bean定义
     * @return 是否为候选组件
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * 检查候选Bean，防止重复定义
     *
     * @param beanName       Bean名称
     * @param beanDefinition Bean定义
     * @return 是否为有效候选
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            Logger.warn(
                    "Skipping MapperFactoryBean with name '{}' and '{}' mapperInterface. Bean already defined with the same name!",
                    beanName, beanDefinition.getBeanClassName());
            return false;
        }
    }

    /**
     * 设置扫描的注解类型
     *
     * @param annotationClass 注解类
     */
    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * 设置MapperFactoryBean
     *
     * @param mapperFactoryBean Mapper工厂Bean
     */
    public void setMapperFactoryBean(MapperFactoryBean<?> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean != null ? mapperFactoryBean : new MapperFactoryBean<>();
    }

    /**
     * 设置MapperBuilder的Bean名称
     *
     * @param mapperBuilderBeanName MapperBuilder的Bean名称
     */
    public void setMapperBuilderBeanName(String mapperBuilderBeanName) {
        this.mapperBuilderBeanName = mapperBuilderBeanName;
    }

    /**
     * 设置标记接口
     *
     * @param markerInterface 标记接口
     */
    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    /**
     * 设置SqlSessionFactory
     *
     * @param sqlSessionFactory SqlSession工厂
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 设置SqlSessionFactory的Bean名称
     *
     * @param sqlSessionFactoryBeanName SqlSessionFactory的Bean名称
     */
    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    /**
     * 设置SqlSessionTemplate
     *
     * @param sqlSessionTemplate SqlSession模板
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 设置SqlSessionTemplate的Bean名称
     *
     * @param sqlSessionTemplateBeanName SqlSessionTemplate的Bean名称
     */
    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateBeanName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateBeanName;
    }

}