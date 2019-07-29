package org.aoju.bus.limiter.source;

import org.aoju.bus.limiter.resource.LimitedResource;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 获取限流规则
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface LimitedResourceSource {

    /**
     * @param targetClass
     * @param method
     * @return
     */
    Collection<LimitedResource> getLimitedResource(Class<?> targetClass, Method method);
}
