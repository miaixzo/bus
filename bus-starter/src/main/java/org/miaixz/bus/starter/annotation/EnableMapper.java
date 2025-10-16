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
 * @since Java 17+
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
     * plugins like PageHelper.
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
