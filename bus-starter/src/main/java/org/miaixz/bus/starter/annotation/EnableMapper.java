/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 * 开启 Mybatis/Mapper
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({JdbcConfiguration.class, MapperScannerRegistrar.class, MapperConfiguration.class})
public @interface EnableMapper {

    /**
     * {@link #basePackages()} 属性的别名，与basePackages有相同效果
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
     * 类型安全的替代{@link #basePackages()} 用于指定要扫描的包以查找带注释的组件,每个指定类的包将被扫描
     * 考虑在每个包中创建一个特殊的无操作标记类或接口，它除了被该属性引用之外没有其他用途。
     *
     * @return the class
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 用于命名Spring容器中检测到的组件的{@link BeanNameGenerator}类
     *
     * @return the class
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * 此属性指定扫描器将搜索的注释
     * 扫描器将在基本包中注册所有同样具有指定注释的接口
     * 注意，这可以与markerInterface结合使用
     *
     * @return the class
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * 此属性指定扫描程序将搜索的父程序
     * 扫描器将注册基包中所有同样具有指定接口类作为父类的接口
     * 注意，这可以与annotationClass结合使用
     *
     * @return the class
     */
    Class<?> markerInterface() default Class.class;

    /**
     * 指定在spring上下文中有多个SqlSessionTemplate时使用哪个{@code SqlSessionTemplate}
     * 通常只有当您有多个数据源时才需要这样做
     *
     * @return the string
     */
    String sqlSessionTemplateRef() default Normal.EMPTY;

    /**
     * 指定在spring上下文中有多个SqlSessionFactory时使用哪个{@code SqlSessionFactory}
     * 通常只有当您有多个数据源时才需要这样做
     *
     * @return the string
     */
    String sqlSessionFactoryRef() default Normal.EMPTY;

    /**
     * 指定一个自定义的MapperFactoryBean来返回一个mybatis代理作为spring bean
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
