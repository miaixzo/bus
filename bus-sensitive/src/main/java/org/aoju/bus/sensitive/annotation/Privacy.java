package org.aoju.bus.sensitive.annotation;

import java.lang.annotation.*;

/**
 * 隐私数据加解密
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Privacy {

}
