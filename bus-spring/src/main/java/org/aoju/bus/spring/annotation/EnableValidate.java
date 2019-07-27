package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.validate.ImportedClassSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 数据校验
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ImportedClassSelector.class})
public @interface EnableValidate {
}
