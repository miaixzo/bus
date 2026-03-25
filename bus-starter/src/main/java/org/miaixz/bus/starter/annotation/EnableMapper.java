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
package org.miaixz.bus.starter.annotation;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.jdbc.JdbcConfiguration;
import org.miaixz.bus.starter.mapper.MapperConfiguration;
import org.miaixz.bus.starter.mapper.MapperFactoryBean;
import org.miaixz.bus.starter.mapper.MapperScannerRegistrar;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables MyBatis and Mapper framework support.
 * <p>
 * This annotation is the primary entry point for configuring the persistence layer. It imports the necessary
 * configurations for both JDBC data sources and the MyBatis Mapper framework, and it triggers the scanning of mapper
 * interfaces.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ JdbcConfiguration.class, MapperScannerRegistrar.class, MapperConfiguration.class })
public @interface EnableMapper {

    /**
     * Alias for the {@link #basePackage()} attribute. Allows for more concise annotation usage.
     *
     * @return An array of base packages to scan.
     */
    String[] value() default {};

    /**
     * Base packages to scan for MyBatis interfaces.
     *
     * @return An array of base packages.
     */
    String[] basePackage() default {};

    /**
     * Type-safe alternative to {@link #basePackage()} for specifying packages to scan. The package of each class
     * specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that serves no purpose other than
     * being referenced by this attribute.
     *
     * @return An array of classes whose packages will be scanned.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * The {@link BeanNameGenerator} class to be used for naming detected components in the Spring container.
     *
     * @return The {@link BeanNameGenerator} class.
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * This property specifies the annotation that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that have this annotation. Note that this can be
     * combined with {@code markerInterface}.
     *
     * @return The annotation class to scan for.
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * This property specifies the parent interface that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that have this interface as a parent. Note that this
     * can be combined with {@code annotationClass}.
     *
     * @return The marker interface to scan for.
     */
    Class<?> markerInterface() default Class.class;

    /**
     * Specifies which {@code SqlSessionTemplate} to use when there are multiple in the Spring context. Usually, this is
     * only needed when you have multiple data sources.
     *
     * @return The bean name of the {@code SqlSessionTemplate}.
     */
    String sqlSessionTemplateRef() default Normal.EMPTY;

    /**
     * Specifies which {@code SqlSessionFactory} to use when there are multiple in the Spring context. Usually, this is
     * only needed when you have multiple data sources.
     *
     * @return The bean name of the {@code SqlSessionFactory}.
     */
    String sqlSessionFactoryRef() default Normal.EMPTY;

    /**
     * Specifies a custom {@link MapperFactoryBean} to return a MyBatis proxy as a Spring bean.
     *
     * @return The custom {@link MapperFactoryBean} class.
     */
    Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

    /**
     * Configuration properties for the generic Mapper, with one property per line. This is often used to configure
     * plugins like PageContext.
     *
     * @return An array of property strings.
     */
    String[] properties() default {};

    /**
     * Allows for the direct configuration of a MapperBuilder bean by reference.
     *
     * @return The bean name of the {@code MapperBuilder}.
     */
    String mapperBuilderRef() default Normal.EMPTY;

}
