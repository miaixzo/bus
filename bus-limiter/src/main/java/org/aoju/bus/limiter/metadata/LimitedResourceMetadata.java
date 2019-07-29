package org.aoju.bus.limiter.metadata;

import org.aoju.bus.limiter.ArgumentInjector;
import org.aoju.bus.limiter.ErrorHandler;
import org.aoju.bus.limiter.LimitedFallbackResolver;
import org.aoju.bus.limiter.Limiter;
import org.aoju.bus.limiter.resource.LimitedResource;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface LimitedResourceMetadata<T extends LimitedResource> {

    Class<?> getTargetClass();

    Method getTargetMethod();

    T getLimitedResource();

    Limiter getLimiter();

    ErrorHandler getErrorHandler();

    LimitedFallbackResolver getFallback();

    Collection<ArgumentInjector> getArgumentInjectors();

    Map<String, Object> getLimiterParameters();

}
