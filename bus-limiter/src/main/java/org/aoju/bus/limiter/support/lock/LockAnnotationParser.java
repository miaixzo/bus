package org.aoju.bus.limiter.support.lock;

import org.aoju.bus.limiter.AbstractLimiterAnnotationParser;
import org.aoju.bus.limiter.annotation.HLock;
import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.core.annotation.AnnotationAttributes;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class LockAnnotationParser extends AbstractLimiterAnnotationParser<Lock, HLock> {

    @Override
    public LimitedResource parseLimiterAnnotation(AnnotationAttributes attributes) {
        return new LockResource(
                getKey(attributes),
                getArgumentInjectors(attributes),
                getFallback(attributes),
                getErrorHandler(attributes),
                getLimiter(attributes)
        );
    }

}
