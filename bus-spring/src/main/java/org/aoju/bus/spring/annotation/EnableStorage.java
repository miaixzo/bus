package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.storage.StorageConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({StorageConfiguration.class})
public @interface EnableStorage {

}
