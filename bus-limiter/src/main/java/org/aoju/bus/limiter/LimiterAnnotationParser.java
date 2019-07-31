package org.aoju.bus.limiter;

import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface LimiterAnnotationParser<T extends Limiter> {

    Class<Annotation> getSupportAnnotation();

    LimitedResource<T> parseLimiterAnnotation(AnnotationAttributes attributes);

}
