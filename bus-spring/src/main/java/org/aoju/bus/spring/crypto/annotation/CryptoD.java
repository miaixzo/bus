package org.aoju.bus.spring.crypto.annotation;

import org.aoju.bus.crypto.Mode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 解密
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CryptoD {

    /**
     * 如果选择的 RSA 加/解密算法，那么 key 为必填项
     *
     * @return Mode
     */
    Mode type() default Mode.AES;

    /**
     * 可选，如果未配置则采用全局的key
     *
     * @return String
     */
    String key() default "";

    /**
     * 描述信息
     *
     * @return String
     */
    String description() default "";

}
