package org.aoju.bus.limiter.support.rate;

import org.aoju.bus.limiter.metadata.AbstractLimitedResourceMetadata;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RateLimiterResourceMetadata extends AbstractLimitedResourceMetadata<RateLimiterResource> {

    public RateLimiterResourceMetadata(RateLimiterResource limitedResource, Class<?> targetClass, Method targetMethod, BeanFactory beanFactory) {
        super(limitedResource, targetClass, targetMethod, beanFactory);
    }

    @Override
    protected void parseInternal(RateLimiterResource limitedResource) {
    }

}
