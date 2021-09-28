/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.annotation;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.starter.druid.DruidConfiguration;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 Mybatis/Mapper
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({DruidConfiguration.class, MapperScannerRegistrar.class, MapperConfiguration.class})
public @interface EnableMapper {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise
     * annotation declarations.
     *
     * @return the array
     */
    String[] value() default {};

    /**
     * 扫描MyBatis接口的基本包
     *
     * @return the string
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     *
     * @return the class
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * The {@link BeanNameGenerator} class to be used for naming detected components
     * within the Spring container.
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * This property specifies the annotation that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also have
     * the specified annotation.
     * <p>
     * Note this can be combined with markerInterface.
     *
     * @return the class
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * This property specifies the parent that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also have
     * the specified interface class as a parent.
     * <p>
     * Note this can be combined with annotationClass.
     *
     * @return the class
     */
    Class<?> markerInterface() default Class.class;

    /**
     * Specifies which {@code SqlSessionTemplate} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     *
     * @return the string
     */
    String sqlSessionTemplateRef() default Normal.EMPTY;

    /**
     * Specifies which {@code SqlSessionFactory} to use in the case that there is
     * more than one in the spring context. Usually this is only needed when you
     * have more than one datasource.
     *
     * @return the string
     */
    String sqlSessionFactoryRef() default Normal.EMPTY;

    /**
     * Specifies a custom MapperFactoryBean to return a mybatis proxy as spring bean.
     *
     * @return the class
     */
    Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

    /**
     * 通用 Mapper 的配置，一行一个配置
     *
     * @return the array
     */
    String[] properties() default {};

    /**
     * 还可以直接配置一个 MapperBuilder bean
     *
     * @return the string
     */
    String mapperBuilderRef() default Normal.EMPTY;

}
