package org.aoju.bus.core.annotation;

import java.lang.annotation.*;

/**
 * 线程安全注解
 * 1.此注解放在类上，标识当前类为线程安全的。
 * 2.此注解放在方法上，标识方法是线程安全的。
 * <p>
 * 注意：目前此注解仅供内部使用，用来标识类是否线程安全。(表示作者的预期) 真正效果需要验证。
 * <p>
 * 后期用途：可能会直接基于 class 进行反射创建，要求有些类需要显示指定这个注解。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadSafe {

}
