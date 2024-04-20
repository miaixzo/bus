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
package org.miaixz.bus.limiter.metadata;

import org.miaixz.bus.core.toolkit.AnnoKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.limiter.Handler;
import org.miaixz.bus.limiter.Injector;
import org.miaixz.bus.limiter.Limiter;
import org.miaixz.bus.limiter.Resolver;
import org.miaixz.bus.limiter.annotation.LimiterParameter;
import org.miaixz.bus.limiter.resource.LimitedResource;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractLimitedResourceMetadata<T extends LimitedResource> implements LimitedResourceMetadata<T>, Observer {

    private final BeanFactory beanFactory;

    private final T limitedResource;

    private Limiter limiter;

    private Handler errorHandler;

    private Resolver limitedFallbackResolver;

    private Collection<Injector> argumentInjectors;

    private Map<String, Object> limiterParameters;

    private Class<?> targetClass;

    private Method targetMethod;


    public AbstractLimitedResourceMetadata(T limitedResource, Class<?> targetClass, Method targetMethod, BeanFactory beanFactory) {
        this.limitedResource = limitedResource;
        this.beanFactory = beanFactory;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        parse(limitedResource);
    }

    protected abstract void parseInternal(T limitedResource);

    private void parse(T limitedResource) {
        this.limiter = (Limiter) this.beanFactory.getBean(limitedResource.getLimiter());
        this.errorHandler = (Handler) this.beanFactory.getBean(limitedResource.getErrorHandler());
        // 优先获取本类中的同名方法 而后从BeanFactory中获取
        try {
            final Method fallbackMethod = this.targetClass.getDeclaredMethod(limitedResource.getFallback(), this.targetMethod.getParameterTypes());
            fallbackMethod.setAccessible(true);
            this.limitedFallbackResolver = (method, clazz, args, limitedResource1, target) -> {
                try {
                    return fallbackMethod.invoke(target, args);
                } catch (IllegalAccessException e) {
                    return null;
                } catch (InvocationTargetException e) {
                    return null;
                }
            };
        } catch (NoSuchMethodException e) {
            this.limitedFallbackResolver = (Resolver) this.beanFactory.getBean(limitedResource.getFallback());
        }

        if (!CollKit.isEmpty(limitedResource.getArgumentInjectors())) {
            argumentInjectors = new ArrayList<>();
            Collection<String> injectors = limitedResource.getArgumentInjectors();
            for (String si : injectors) {
                argumentInjectors.add((Injector) this.beanFactory.getBean(si));
            }
        }
        this.limiterParameters = findLimiterParameters();
        this.parseInternal(limitedResource);
        if (limitedResource instanceof Observable) {
            ((Observable) limitedResource).addObserver(this);
        }

    }

    private Map<String, Object> findLimiterParameters() {
        // 获取所有LimiterParameter标记的字段
        Field[] fields = this.getLimitedResource().getClass().getDeclaredFields();
        Map<String, Object> retVal = null;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (AnnoKit.hasAnnotation(field, LimiterParameter.class)) {
                if (null == retVal) {
                    retVal = new HashMap<>();
                }
                try {
                    retVal.put(field.getName(), field.get(this.getLimitedResource()));
                } catch (IllegalAccessException e) {
                }
            }
        }
        return retVal;
    }

    @Override
    public Map<String, Object> getLimiterParameters() {
        return limiterParameters;
    }

    @Override
    public T getLimitedResource() {
        return this.limitedResource;
    }

    @Override
    public void update(Observable o, Object arg) {
        parse(this.limitedResource);
    }


    @Override
    public Limiter getLimiter() {
        return limiter;
    }

    @Override
    public Resolver getFallback() {
        return limitedFallbackResolver;
    }

    @Override
    public Handler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public Collection<Injector> getArgumentInjectors() {
        return argumentInjectors;
    }


    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    @Override
    public Method getTargetMethod() {
        return targetMethod;
    }
}
