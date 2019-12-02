package org.aoju.bus.starter.annotation;

import org.aoju.bus.starter.storage.StorageConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用OSS存储
 *
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({StorageConfiguration.class})
public @interface EnableStorage {

}
