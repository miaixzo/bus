package org.aoju.bus.sensitive.annotation;

import java.lang.annotation.*;

/**
 * 如果对象中属性为另外一个对象(集合)，则可以使用这个注解指定。
 * 1. 如果属性为 Iterable 的子类集合，则当做列表处理，遍历其中的对象
 * 2. 如果是普通对象，则处理对象中的脱敏信息
 * 3. 如果是普通字段/MAP，则不做处理
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entry {

}
