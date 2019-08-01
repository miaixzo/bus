package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.druid.DruidConfiguration;
import org.aoju.bus.spring.druid.DruidMonitorConfiguration;
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
@Import(value = {DruidConfiguration.class, DruidMonitorConfiguration.class})
public @interface EnableDruids {

}