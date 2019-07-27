package org.aoju.bus.limiter;

import org.aoju.bus.limiter.resource.LimitedResource;

import java.lang.reflect.Method;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface LimitedFallbackResolver<T> {

    T resolve(Method method, Class<?> clazz, Object[] args, LimitedResource limitedResource, Object target);

}
