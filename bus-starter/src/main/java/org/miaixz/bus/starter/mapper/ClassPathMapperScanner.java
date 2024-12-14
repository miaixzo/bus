/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.entity.Property;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
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

    private boolean addToConfig = true;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private String sqlSessionTemplateBeanName;

    private String sqlSessionFactoryBeanName;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private MapperBuilder mapperBuilder;

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
     * @param basePackages 扫描路径
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            Logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages)
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
            } else {
                // 不做任何配置的时候使用默认方式
                if (this.mapperBuilder == null) {
                    this.mapperBuilder = new MapperBuilder();
                }
                definition.getPropertyValues().add("mapperBuilder", this.mapperBuilder);
            }
            definition.getPropertyValues().add("addToConfig", this.addToConfig);

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

    public MapperBuilder getMapperBuilder() {
        return mapperBuilder;
    }

    public void setMapperBuilder(MapperBuilder mapperBuilder) {
        this.mapperBuilder = mapperBuilder;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * 配置通用 Mapper
     *
     * @param property 配置信息
     */
    public void setConfig(Property property) {
        if (mapperBuilder == null) {
            mapperBuilder = new MapperBuilder();
        }
        mapperBuilder.setConfig(property);
    }

    public void setMapperFactoryBean(MapperFactoryBean<?> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean != null ? mapperFactoryBean : new MapperFactoryBean<>();
    }

    public void setMapperBuilderBeanName(String mapperBuilderBeanName) {
        this.mapperBuilderBeanName = mapperBuilderBeanName;
    }

    /**
     * 从环境变量中获取 mapper 配置信息
     *
     * @param environment 环境配置信息
     */
    public void setMapperProperties(Environment environment) {
        try {
            Property property = PlaceHolderBinder.bind(environment, Property.class, GeniusBuilder.MYBATIS);
            if (mapperBuilder == null) {
                mapperBuilder = new MapperBuilder();
            }
            if (property != null) {
                mapperBuilder.setConfig(property);
            }
        } catch (Exception e) {
            Logger.warn("只有 Spring Boot 环境中可以通过 Environment(配置文件,环境变量,运行参数等方式) 配置通用 Mapper，"
                    + "其他环境请通过 @EnableMapper 注解中的 mapperBuilderRef 或 properties 参数进行配置!"
                    + "当然,如果你使用 org.miaixz.bus..mapper.Property 配置的通用 Mapper，可以忽略该警告!", e);
        }
    }

    /**
     * 从 properties 数组获取 mapper 配置信息
     *
     * @param properties 属性配置信息
     */
    public void setMapperProperties(String[] properties) {
        if (mapperBuilder == null) {
            mapperBuilder = new MapperBuilder();
        }
        Properties props = new Properties();
        for (String property : properties) {
            property = property.trim();
            int index = property.indexOf(Symbol.EQUAL);
            if (index < 0) {
                throw new InternalException("通过 @EnableMapper 注解的 properties 参数配置出错:" + property + " !\n"
                        + "请保证配置项按 properties 文件格式要求进行配置，例如：\n" + "properties = {\n"
                        + "\t\"mappers=org.miaixz.bus.mapper.Mapper\",\n" + "\t\"notEmpty=true\"\n" + "}");
            }
            props.put(property.substring(0, index).trim(), property.substring(index + 1).trim());
        }
        mapperBuilder.setProperties(props);
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
