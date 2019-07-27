package org.aoju.bus.spring.druid;

import java.lang.annotation.*;


/**
 * 多数据源支持
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    String value() default "dataSource";

}