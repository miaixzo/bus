package org.aoju.bus.limiter.source;

import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.limiter.LimiterAnnotationParser;
import org.aoju.bus.limiter.resource.LimitedResource;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DefaultLimitedResourceSource implements LimitedResourceSource {

    private static final Collection<LimitedResource> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

    private final Map<Object, Collection<LimitedResource>> cache = new ConcurrentHashMap(1024);

    private final Set<LimiterAnnotationParser> annotationParsers;


    public DefaultLimitedResourceSource(LimiterAnnotationParser... annotationParsers) {
        Set<LimiterAnnotationParser> parsers = new LinkedHashSet<>(annotationParsers.length);
        Collections.addAll(parsers, annotationParsers);
        this.annotationParsers = parsers;
    }

    @Override
    public Collection<LimitedResource> getLimitedResource(Class<?> targetClass, Method method) {
        MethodClassKey key = new MethodClassKey(method, targetClass);
        Collection<LimitedResource> retVal = cache.get(key);
        if (retVal != null) {
            return retVal;
        }
        retVal = computeLimitedResource(method, targetClass);
        if (CollUtils.isEmpty(retVal)) {
            cache.put(key, NULL_CACHING_ATTRIBUTE);
            return null;
        } else {
            cache.put(key, retVal);
            return retVal;
        }
    }

    private Collection<LimitedResource> computeLimitedResource(Method method, Class<?> targetClass) {
        // 从代理前的方法上获取
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        Collection<LimitedResource> reDef = findLimitedResource(specificMethod);
        if (!CollUtils.isEmpty(reDef)) {
            return reDef;
        }
        // 代理前class对象
        reDef = findLimitedResource(specificMethod.getDeclaringClass());
        if (!CollUtils.isEmpty(reDef) && ClassUtils.isUserLevelMethod(specificMethod)) {
            return reDef;
        }
        if (specificMethod != method) {
            // 代理后的方法
            reDef = findLimitedResource(method);
            if (!CollUtils.isEmpty(reDef)) {
                return reDef;
            }
            // 代理后的class对象
            reDef = findLimitedResource(method.getDeclaringClass());
            if (!CollUtils.isEmpty(reDef) && ClassUtils.isUserLevelMethod(method)) {
                return reDef;
            }
        }

        return null;
    }


    private Collection<LimitedResource> findLimitedResource(Method method) {
        return findLimitedResourceFromAnnotatedElement(method);
    }

    private Collection<LimitedResource> findLimitedResource(Class clazz) {
        return findLimitedResourceFromAnnotatedElement(clazz);
    }


    private Collection<LimitedResource> findLimitedResourceFromAnnotatedElement(AnnotatedElement ae) {
        Annotation[] annotations = ae.getAnnotations();
        Collection<LimitedResource> retVal = null;
        for (LimiterAnnotationParser parser : annotationParsers) {
            for (Annotation ai : annotations) {
                if (ai.annotationType().equals(parser.getSupportAnnotation())) {
                    if (retVal == null) {
                        retVal = new ArrayList<>();
                    }
                    AnnotationAttributes attributes = AnnotationUtils.getAnnotationAttributes(ae, ai);
                    retVal.add(parser.parseLimiterAnnotation(attributes));
                }

            }
        }
        return retVal;
    }

}
