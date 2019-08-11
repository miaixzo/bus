/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.druid.DruidConfiguration;
import org.aoju.bus.spring.mapper.MapperConfiguration;
import org.aoju.bus.spring.mapper.MapperFactoryBean;
import org.aoju.bus.spring.mapper.MybatisConfiguration;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 Mybatis/Mapper
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@Import(value = {MybatisConfiguration.class, MapperConfiguration.class, DruidConfiguration.class})
public @interface EnableMapper {

    /**
     * {@link #basePackageClass()}属性的别名。允许更简洁
     * 注释声明，例如:
     * {@code @EnableMapper("org.aoju.pkg")}
     * {@code @EnableMapper(basePackage = {"org.aoju.pkg"})}
     *
     * @return the array
     */
    String[] value() default {};

    /**
     * 扫描MyBatis接口的基本包。注意，用户接口标记等
     *
     * @return the array
     */
    String[] basePackage() default {};

    /**
     * 类型安全替代{@link #basePackage()}指定包扫描带注释的组件。将扫描指定的每个类的包。
     * 考虑在每个包中创建一个特殊的标记类或接口
     *
     * @return the object
     */
    Class<?>[] basePackageClass() default {};

    /**
     * 用于命名检测到的组件的{@link BeanNameGenerator}类。
     *
     * @return the object
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * 此属性指定扫描器将搜索的注释。
     * 扫描器将在基本包中注册所有接口指定的注释。
     * 注意，这可以与markerInterface相结合。
     *
     * @return the object
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * 此属性指定扫描程序将搜索的父级。
     * 扫描器将注册基本包中也具有指定的接口类作为父级。
     * 注意，可以与annotationClass结合使用。
     *
     * @return the object
     */
    Class<?> markerInterface() default Class.class;

    /**
     * 指定数据源信息,有多数据源时可使用或指定对应多 SqlSessionTemplate。
     *
     * @return the string
     */
    String sqlSessionTemplate() default "";

    /**
     * 指定数据源信息,有多数据源时可使用或指定对应多 sqlSessionFactory。
     *
     * @return the string
     */
    String sqlSessionFactory() default "";

    /**
     * 指定自定义MapperFactoryBean以将MyBatis代理返回为SpringBean
     *
     * @return the object
     */
    Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

    /**
     * 通用 Mapper 的配置，一行一个配置
     *
     * @return the string
     */
    String[] properties() default {};

    /**
     * 还可以直接配置一个 MapperHelper bean
     *
     * @return the string
     */
    String mapperHelper() default "";

}
