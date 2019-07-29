package org.aoju.bus.boot.cache;


import org.aoju.bus.spring.cache.CacheConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Cache 自动配置
 */
@Configuration
@Import(value = CacheConfiguration.class)
public class CacheAutoConfiguration {

}
