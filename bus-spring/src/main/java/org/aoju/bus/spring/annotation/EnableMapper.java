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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
     * {@code @EnableMapper(basePackage = {"org.aoju.pkg"})}。
     */
    String[] value() default {};

    /**
     * 扫描MyBatis接口的基本包。注意，用户接口标记等。
     */
    String[] basePackage() default {};

    /**
     * 类型安全替代{@link #basePackage()}指定包扫描带注释的组件。将扫描指定的每个类的包。
     * 考虑在每个包中创建一个特殊的标记类或接口
     */
    Class<?>[] basePackageClass() default {};

    /**
     * 用于命名检测到的组件的{@link BeanNameGenerator}类。
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * 此属性指定扫描器将搜索的注释。
     * 扫描器将在基本包中注册所有接口指定的注释。
     * 注意，这可以与markerInterface相结合。
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * 此属性指定扫描程序将搜索的父级。
     * 扫描器将注册基本包中也具有指定的接口类作为父级。
     * 注意，可以与annotationClass结合使用。
     */
    Class<?> markerInterface() default Class.class;

    /**
     * 指定数据源信息,有多数据源时可使用或指定对应多 SqlSessionTemplate。
     */
    String sqlSessionTemplate() default "";

    /**
     * 指定数据源信息,有多数据源时可使用或指定对应多 sqlSessionFactory。
     */
    String sqlSessionFactory() default "";

    /**
     * 指定自定义MapperFactoryBean以将MyBatis代理返回为SpringBean
     */
    Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

    /**
     * 通用 Mapper 的配置，一行一个配置
     */
    String[] properties() default {};

    /**
     * 还可以直接配置一个 MapperHelper bean
     */
    String mapperHelper() default "";

}