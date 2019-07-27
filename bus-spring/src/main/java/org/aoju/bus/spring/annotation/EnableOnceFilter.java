package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.servlet.RequestWrapperFilter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 Request/Response 多次读取
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
@Import(RequestWrapperFilter.class)
public @interface EnableOnceFilter {

}
