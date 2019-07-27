package org.aoju.bus.limiter.support.rate;

import org.aoju.bus.limiter.annotation.LimiterParameter;
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
public class RateLimiterResource extends AbstractLimitedResource {

    @LimiterParameter
    private double rate;

    @LimiterParameter
    private long capacity;

    public RateLimiterResource(String key, Collection<String> argumentInjectors, String fallback, String errorHandler, String limiter, double rate, long capacity) {
        super(key, argumentInjectors, fallback, errorHandler, limiter);
        this.rate = rate;
        this.capacity = capacity;
    }

    @Override
    public LimitedResourceMetadata createMetadata(BeanFactory beanFactory, Class targetClass, Method targetMethod) {
        return new RateLimiterResourceMetadata(this, targetClass, targetMethod, beanFactory);
    }

}
