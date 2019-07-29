package org.aoju.bus.limiter.resource;

import org.aoju.bus.limiter.Limiter;
import org.aoju.bus.limiter.metadata.LimitedResourceMetadata;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface LimitedResource<T extends Limiter> {

    String getKey();

    String getLimiter();

    String getFallback();

    String getErrorHandler();

    Collection<String> getArgumentInjectors();

    LimitedResourceMetadata createMetadata(BeanFactory beanFactory, Class<?> targetClass, Method targetMethod);

}
