package org.aoju.bus.core.annotation.env;

import java.lang.annotation.*;

/**
 * 生产环境
 * 表示当前方法性能安全性各方面达标，可以用于生产环境。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Prod {
}
