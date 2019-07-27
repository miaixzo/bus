package org.aoju.bus.cache.support;

import org.aoju.bus.cache.annotation.CacheKey;
import org.aoju.bus.cache.entity.CacheHolder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PatternGenerator {

    private static final ConcurrentMap<Method, String> patterns = new ConcurrentHashMap<>();

    public static String generatePattern(CacheHolder cacheHolder) {
        return patterns.computeIfAbsent(cacheHolder.getMethod(), (method) -> doPatternCombiner(cacheHolder));
    }

    private static String doPatternCombiner(CacheHolder cacheHolder) {
        StringBuilder sb = new StringBuilder(cacheHolder.getPrefix());
        Collection<CacheKey> cacheKeys = cacheHolder.getCacheKeyMap().values();
        for (CacheKey cacheKey : cacheKeys) {
            sb.append(cacheKey.value());
        }

        return sb.toString();
    }
}
