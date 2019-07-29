package org.aoju.bus.cache;

import org.aoju.bus.cache.provider.BaseProvider;
import org.aoju.bus.cache.support.cache.Cache;

import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CacheConfig {

    // cache接口实现
    private Map<String, Cache> caches;

    // 缓存分组命中率统计
    private BaseProvider provider;

    // 是否开启Cache(全局开关)
    private Switch cache;

    // 是否开启缓存防击穿
    private Switch prevent;

    public static CacheConfig newConfig(Map<String, Cache> caches) {
        CacheConfig config = new CacheConfig();
        config.caches = caches;
        config.cache = Switch.ON;
        config.prevent = Switch.OFF;
        config.provider = null;
        return config;
    }

    public boolean isPreventOn() {
        return prevent != null && prevent == Switch.ON;
    }

    public Map<String, Cache> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, Cache> caches) {
        this.caches = caches;
    }

    public BaseProvider getProvider() {
        return provider;
    }

    public void setProvider(BaseProvider provider) {
        this.provider = provider;
    }

    public Switch getCache() {
        return cache;
    }

    public void setCache(Switch cache) {
        this.cache = cache;
    }

    public Switch getPrevent() {
        return prevent;
    }

    public void setPrevent(Switch prevent) {
        this.prevent = prevent;
    }

    public enum Switch {
        ON,
        OFF
    }

}
