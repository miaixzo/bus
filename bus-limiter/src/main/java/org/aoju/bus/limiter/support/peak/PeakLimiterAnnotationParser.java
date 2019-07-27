package org.aoju.bus.limiter.support.peak;

import org.aoju.bus.limiter.AbstractLimiterAnnotationParser;
import org.aoju.bus.limiter.annotation.HPeak;
import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.core.annotation.AnnotationAttributes;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PeakLimiterAnnotationParser extends AbstractLimiterAnnotationParser<PeakLimiter, HPeak> {

    @Override
    public LimitedResource<PeakLimiter> parseLimiterAnnotation(AnnotationAttributes attributes) {

        return new PeakLimiterResource(
                getKey(attributes),
                getArgumentInjectors(attributes),
                getFallback(attributes),
                getErrorHandler(attributes),
                getLimiter(attributes),
                attributes.getNumber("max")
        );
    }

}
