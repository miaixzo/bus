package org.aoju.bus.spring.cache;

import org.aoju.bus.cache.support.cache.Cache;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 缓存相关配置
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
@ConfigurationProperties("request.cache")
public class CacheProperties {

    private String type;
    private Map<String, Cache> map;

}
