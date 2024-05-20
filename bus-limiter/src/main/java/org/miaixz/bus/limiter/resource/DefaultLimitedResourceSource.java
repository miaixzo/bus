/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.limiter.resource;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.limiter.Parser;
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
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultLimitedResourceSource implements LimitedResourceSource {

    private static final Collection<LimitedResource> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

    private final Map<Object, Collection<LimitedResource>> cache = new ConcurrentHashMap(Normal._1024);

    private final Set<Parser> annotationParsers;


    public DefaultLimitedResourceSource(Parser... annotationParsers) {
        Set<Parser> parsers = new LinkedHashSet<>(annotationParsers.length);
        Collections.addAll(parsers, annotationParsers);
        this.annotationParsers = parsers;
    }

    @Override
    public Collection<LimitedResource> getLimitedResource(Class<?> targetClass, Method method) {
        MethodClassKey key = new MethodClassKey(method, targetClass);
        Collection<LimitedResource> retVal = cache.get(key);
        if (null != retVal) {
            return retVal;
        }
        retVal = computeLimitedResource(method, targetClass);
        if (CollKit.isEmpty(retVal)) {
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
        if (!CollKit.isEmpty(reDef)) {
            return reDef;
        }
        // 代理前class对象
        reDef = findLimitedResource(specificMethod.getDeclaringClass());
        if (!CollKit.isEmpty(reDef) && MethodKit.isUserLevelMethod(specificMethod)) {
            return reDef;
        }
        if (specificMethod != method) {
            // 代理后的方法
            reDef = findLimitedResource(method);
            if (!CollKit.isEmpty(reDef)) {
                return reDef;
            }
            // 代理后的class对象
            reDef = findLimitedResource(method.getDeclaringClass());
            if (!CollKit.isEmpty(reDef) && MethodKit.isUserLevelMethod(method)) {
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
        for (Parser parser : annotationParsers) {
            for (Annotation ai : annotations) {
                if (ai.annotationType().equals(parser.getSupportAnnotation())) {
                    if (null == retVal) {
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
