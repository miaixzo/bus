package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.dubbo.DubboConfiguration;
import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Dubbo 支持
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@EnableDubboConfig
@DubboComponentScan
@Import(DubboConfiguration.class)
public @interface EnableDubbo {

    /**
     * Base packages to scan for annotated @Service classes.
     *
     * @return the base packages to scan
     * @see DubboComponentScan#basePackages()
     */
    @AliasFor(annotation = DubboComponentScan.class, attribute = "basePackages")
    String[] basePackage() default {};

    /**
     * packages to scan for annotated @Service classes.
     * The package of each class specified will be scanned.
     *
     * @return classes from the base packages to scan
     * @see DubboComponentScan#basePackageClasses
     */
    @AliasFor(annotation = DubboComponentScan.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClass() default {};


    /**
     * binding to multiple Spring Beans.
     *
     * @return the default value is <code>true</code>
     * @see EnableDubboConfig#multiple()
     */
    @AliasFor(annotation = EnableDubboConfig.class, attribute = "multiple")
    boolean multiple() default true;

}