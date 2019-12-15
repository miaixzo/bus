package org.aoju.bus.starter.annotation;

import org.aoju.bus.starter.cache.CacheConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用缓存
 *
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({CacheConfiguration.class})
public @interface EnableCache {

}
