package org.aoju.bus.spring.annotation;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.aoju.bus.spring.druid.DruidConfiguration;
import org.springframework.context.annotation.Import;

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
@Import(DruidConfiguration.class)
public @interface EnableDruid {

}