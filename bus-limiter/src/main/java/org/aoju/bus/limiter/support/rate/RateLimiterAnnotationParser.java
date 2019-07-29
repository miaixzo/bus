package org.aoju.bus.limiter.support.rate;

import org.aoju.bus.limiter.AbstractLimiterAnnotationParser;
import org.aoju.bus.limiter.annotation.HRateLimiter;
import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.core.annotation.AnnotationAttributes;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RateLimiterAnnotationParser extends AbstractLimiterAnnotationParser<RateLimiter, HRateLimiter> {

    @Override
    public LimitedResource<RateLimiter> parseLimiterAnnotation(AnnotationAttributes attributes) {
        return new RateLimiterResource(getKey(attributes),
                getArgumentInjectors(attributes),
                getFallback(attributes),
                getErrorHandler(attributes),
                getLimiter(attributes),
                attributes.getNumber("rate"),
                attributes.getNumber("capacity")
        );
    }

}
