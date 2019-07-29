package org.aoju.bus.limiter.support.peak;

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
public class PeakLimiterResource extends AbstractLimitedResource {

    @LimiterParameter
    private int max;

    public PeakLimiterResource(String key, Collection<String> argumentInjectors, String fallback, String errorHandler, String limiter, int max) {
        super(key, argumentInjectors, fallback, errorHandler, limiter);
        this.max = max;
    }

    @Override
    public LimitedResourceMetadata createMetadata(BeanFactory beanFactory, Class targetClass, Method targetMethod) {
        return new PeakLimiterResourceMetadata(this, targetClass, targetMethod, beanFactory);
    }
}
