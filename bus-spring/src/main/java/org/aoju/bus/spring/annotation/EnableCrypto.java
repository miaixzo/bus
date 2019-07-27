package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.crypto.CryptoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启加/解密功能
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
@Import({CryptoConfiguration.class})
public @interface EnableCrypto {

}
