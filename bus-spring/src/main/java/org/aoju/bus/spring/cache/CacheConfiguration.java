package org.aoju.bus.spring.cache;

import org.aoju.bus.cache.CacheAspect;
import org.aoju.bus.cache.CacheConfig;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.cache.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存相关配置
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {CacheProperties.class})
public class CacheConfiguration {

    @Autowired
    CacheProperties properties;

    @Autowired
    CacheAspect cacheAspect = cacheConfigurer();

    @Bean
    public CacheAspect cacheConfigurer() {
        String type = StringUtils.toString(this.properties.getType());
        try {
            if (!StringUtils.isEmpty(type)) {
                Object provider = ClassUtils.loadClass(type);
                CacheConfig config = CacheConfig.newConfig(this.properties.getMap());
                if (provider instanceof H2Provider) {
                    // config.setProvider(new H2Provider());
                } else if (provider instanceof MySQLProvider) {
                    //config.setProvider(new MySQLProvider());
                } else if (provider instanceof SqliteProvider) {
                    //config.setProvider(new SqliteProvider());
                } else if (provider instanceof ZKProvider) {
                    //config.setProvider(new ZKProvider());
                } else if (provider instanceof MemoryProvider) {
                    //config.setProvider(new MemoryProvider());
                }
                this.cacheAspect = new CacheAspect(config);
                return this.cacheAspect;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("can not resolve class with type: " + type);
        }
        return null;
    }

}
