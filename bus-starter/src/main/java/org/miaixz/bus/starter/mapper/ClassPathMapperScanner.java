/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.apache.ibatis.session.SqlSessionFactory;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * A {@link ClassPathBeanDefinitionScanner} that scans for and registers MyBatis mapper interfaces.
 * <p>
 * This scanner can be configured to search for interfaces that are annotated with a specific annotation or that extend
 * a specific marker interface. If no annotation or marker interface is specified, it will scan for all interfaces.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

    /**
     * The SqlSessionFactory used to create SqlSessions.
     */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * The SqlSessionTemplate that provides a thread-safe SqlSession.
     */
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * The bean name of the SqlSessionTemplate, for referencing within the Spring container.
     */
    private String sqlSessionTemplateBeanName;

    /**
     * The bean name of the SqlSessionFactory, for referencing within the Spring container.
     */
    private String sqlSessionFactoryBeanName;

    /**
     * The annotation to scan for. Interfaces with this annotation will be registered as mappers.
     */
    private Class<? extends Annotation> annotationClass;

    /**
     * The marker interface to scan for. Interfaces extending this marker will be registered as mappers.
     */
    private Class<?> markerInterface;

    /**
     * The bean name of the MapperBuilder, used for configuring the generic Mapper.
     */
    private String mapperBuilderBeanName;

    /**
     * The factory bean used to create mapper instances.
     */
    private MapperFactoryBean<?> mapperFactoryBean = new MapperFactoryBean<>();

    /**
     * Constructs a new ClassPathMapperScanner.
     *
     * @param registry The Spring bean definition registry.
     */
    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * Configures the scanning filters. It sets up rules to search for interfaces based on annotations or a marker
     * interface. By default, it scans all interfaces and excludes package-info.java.
     */
    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // Add annotation filter if specified.
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        // Add marker interface filter, ignoring the marker interface itself.
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {

                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        // If no specific filters are set, include all interfaces.
        if (acceptAllInterfaces) {
            addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        }

        // Exclude package-info.java files.
        addExcludeFilter(
                (metadataReader, metadataReaderFactory) -> metadataReader.getClassMetadata().getClassName()
                        .endsWith("package-info"));
    }

    /**
     * Scans the specified base packages for mapper interfaces and registers them as beans. It logs a warning if no
     * mappers are found.
     *
     * @param basePackages The base packages to scan.
     * @return A set of BeanDefinitionHolders for the registered beans.
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            Logger.warn(
                    false,
                    "Mapper",
                    "No MyBatis mapper found in '{}' package. Please check configuration",
                    Arrays.toString(basePackages));
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    /**
     * Processes the scanned bean definitions, configuring them as {@link MapperFactoryBean} instances. It sets the
     * mapper interface, SqlSessionFactory/SqlSessionTemplate, and other properties.
     *
     * @param beanDefinitions The set of scanned bean definitions.
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            // The mapper interface is the original class of the bean, but the actual bean class is MapperFactoryBean.
            // definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            definition.setBeanClass(this.mapperFactoryBean.getClass());
            definition.getPropertyValues().add("mapperInterface", beanClassName);

            // Set the generic Mapper builder if specified.
            if (StringKit.hasText(this.mapperBuilderBeanName)) {
                definition.getPropertyValues()
                        .add("mapperBuilder", new RuntimeBeanReference(this.mapperBuilderBeanName));
            }

            boolean explicitFactoryUsed = false;
            if (StringKit.hasText(this.sqlSessionFactoryBeanName)) {
                definition.getPropertyValues()
                        .add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
                explicitFactoryUsed = true;
            } else if (this.sqlSessionFactory != null) {
                definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
                explicitFactoryUsed = true;
            }

            if (StringKit.hasText(this.sqlSessionTemplateBeanName)) {
                if (explicitFactoryUsed) {
                    Logger.warn(
                            false,
                            "Mapper",
                            "Cannot use both sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored");
                }
                definition.getPropertyValues()
                        .add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
                explicitFactoryUsed = true;
            } else if (this.sqlSessionTemplate != null) {
                if (explicitFactoryUsed) {
                    Logger.warn(
                            false,
                            "Mapper",
                            "Cannot use both sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored");
                }
                definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
                explicitFactoryUsed = true;
            }

            // If no explicit factory was configured, explicitly wire to the default 'sqlSessionFactory' bean.
            // This is the most robust solution for AOT compatibility.
            if (!explicitFactoryUsed) {
                definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference("sqlSessionFactory"));
            }
        }
    }

    /**
     * Determines if a bean definition is a candidate for a mapper. It must be an interface and independent.
     *
     * @param beanDefinition The bean definition to check.
     * @return {@code true} if it is a candidate component, {@code false} otherwise.
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * Checks if a bean with the given name is already defined to prevent duplicates.
     *
     * @param beanName       The name of the bean.
     * @param beanDefinition The definition of the bean.
     * @return {@code true} if it is a valid candidate, {@code false} otherwise.
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            Logger.warn(
                    false,
                    "Mapper",
                    "Skipping MapperFactoryBean '{}' with '{}' mapperInterface. Bean already defined with same name",
                    beanName,
                    beanDefinition.getBeanClassName());
            return false;
        }
    }

    /**
     * Sets the annotation class to scan for.
     *
     * @param annotationClass The annotation class.
     */
    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Sets the MapperFactoryBean to be used by the scanner.
     *
     * @param mapperFactoryBean The MapperFactoryBean instance.
     */
    public void setMapperFactoryBean(MapperFactoryBean<?> mapperFactoryBean) {
        this.mapperFactoryBean = mapperFactoryBean != null ? mapperFactoryBean : new MapperFactoryBean<>();
    }

    /**
     * Sets the bean name of the MapperBuilder.
     *
     * @param mapperBuilderBeanName The bean name of the MapperBuilder.
     */
    public void setMapperBuilderBeanName(String mapperBuilderBeanName) {
        this.mapperBuilderBeanName = mapperBuilderBeanName;
    }

    /**
     * Sets the marker interface to scan for.
     *
     * @param markerInterface The marker interface.
     */
    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    /**
     * Sets the SqlSessionFactory to be used by the mappers.
     *
     * @param sqlSessionFactory The SqlSessionFactory instance.
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Sets the bean name of the SqlSessionFactory.
     *
     * @param sqlSessionFactoryBeanName The bean name of the SqlSessionFactory.
     */
    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    /**
     * Sets the SqlSessionTemplate to be used by the mappers.
     *
     * @param sqlSessionTemplate The SqlSessionTemplate instance.
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * Sets the bean name of the SqlSessionTemplate.
     *
     * @param sqlSessionTemplateBeanName The bean name of the SqlSessionTemplate.
     */
    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateBeanName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateBeanName;
    }

}
