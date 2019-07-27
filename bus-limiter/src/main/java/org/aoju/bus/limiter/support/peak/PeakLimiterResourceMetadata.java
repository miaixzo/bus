package org.aoju.bus.limiter.support.peak;

import org.aoju.bus.limiter.metadata.AbstractLimitedResourceMetadata;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PeakLimiterResourceMetadata extends AbstractLimitedResourceMetadata<PeakLimiterResource> {

    public PeakLimiterResourceMetadata(PeakLimiterResource limitedResource, Class<?> targetClass, Method targetMethod, BeanFactory beanFactory) {
        super(limitedResource, targetClass, targetMethod, beanFactory);
    }

    @Override
    protected void parseInternal(PeakLimiterResource limitedResource) {
    }

}
