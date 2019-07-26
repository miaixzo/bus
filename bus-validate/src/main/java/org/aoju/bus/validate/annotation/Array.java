package org.aoju.bus.validate.annotation;

import java.lang.annotation.*;

/**
 * 检查是否为数组或集合
 *
 * <p>
 * 在任意校验注解类型加上{@code @Array}注解， 会在运行时对该校验对象的类型进行判断<br>
 * 如果是数组或者集合对象，则会对内部元素执行当前所有其他的校验器将， 否则执行对校验对象执行校验器
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
public @interface Array {

}
