package org.aoju.bus.sensitive.annotation;

import java.lang.annotation.*;

/**
 * 数据脱敏
 * 1. 所有的注解都要继承这个注解
 * 2. 如果一个字段上面有多个注解，则根据注解的顺序，依次执行。
 * 设计的考虑：
 * 本来想过将生效条件单独抽离为一个注解，这样可以达到条件注解的复用。
 * 但是有一个缺点，当指定多个策略时，条件的注解就会太宽泛，无法保证精细到每一个策略生效的场景。
 * 平衡的方式：
 * 在 support 注解中，可以指定策略。默认是全部，如果指定，则只针对其中的某个策略生效。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /**
     * 脱敏属性
     */
    String[] value() default {};

}
