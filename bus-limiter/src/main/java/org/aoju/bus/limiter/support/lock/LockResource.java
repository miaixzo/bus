package org.aoju.bus.limiter.support.lock;

import org.aoju.bus.limiter.metadata.LimitedResourceMetadata;
import org.aoju.bus.limiter.resource.AbstractLimitedResource;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class LockResource extends AbstractLimitedResource {

    public LockResource(String key, Collection<String> argumentInjectors, String fallback, String errorHandler, String limiter) {
        super(key, argumentInjectors, fallback, errorHandler, limiter);
    }

    @Override
    public LimitedResourceMetadata createMetadata(BeanFactory beanFactory, Class targetClass, Method targetMethod) {
        return new LockResourceMetadata(this, targetClass, targetMethod, beanFactory);
    }

}
