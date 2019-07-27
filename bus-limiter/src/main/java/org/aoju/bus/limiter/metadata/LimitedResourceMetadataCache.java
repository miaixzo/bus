package org.aoju.bus.limiter.metadata;

import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class LimitedResourceMetadataCache {

    private final Map<LimitedResourceKey, LimitedResourceMetadata> metadataCache = new ConcurrentHashMap<>(1024);

    private BeanFactory beanFactory;

    public LimitedResourceMetadataCache(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public LimitedResourceMetadata getLimitedResourceMetadata(LimitedResource limitedResource, Method method, Class<?> targetClass) {
        LimitedResourceKey limitedResourceKey = new LimitedResourceKey(limitedResource, method, targetClass);
        LimitedResourceMetadata metadata = this.metadataCache.get(limitedResourceKey);
        if (metadata == null) {
            metadata = limitedResource.createMetadata(this.beanFactory, targetClass, method);
            this.metadataCache.put(limitedResourceKey, metadata);
        }
        return metadata;
    }

}
