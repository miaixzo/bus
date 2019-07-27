package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.sensitive.ImportedClassSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启请求内容脱敏处理
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
public @interface EnableSensitive {

}
