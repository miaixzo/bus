package org.aoju.bus.core.annotation.env;

import java.lang.annotation.*;

/**
 * 开发环境
 * 表示当前方法禁止用于生产环境，仅可用于开发测试。
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
public @interface Dev {

}
