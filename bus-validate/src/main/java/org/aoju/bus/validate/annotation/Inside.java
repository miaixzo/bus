package org.aoju.bus.validate.annotation;

import java.lang.annotation.*;

/**
 * 标记注解，是否校验对象内部。
 * <p>
 * 在方法参数或对象类型或字段上可以使用，表示遇到校验器遇到该对象时，会尝试校验对象内部的所有字段
 * </P>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inside {

}
