package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.time.TimeZoneConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 JDK8 日期格式化
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
@Import(TimeZoneConfig.class)
public @interface EnableTimeZone {

}
