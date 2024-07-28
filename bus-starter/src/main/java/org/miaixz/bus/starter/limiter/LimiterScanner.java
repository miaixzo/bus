/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.limiter;

import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.limiter.Builder;
import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.limiter.magic.annotation.Downgrade;
import org.miaixz.bus.limiter.magic.annotation.Hotspot;
import org.miaixz.bus.limiter.magic.annotation.Limiting;
import org.miaixz.bus.limiter.metric.MethodManager;
import org.miaixz.bus.limiter.metric.StrategyManager;
import org.miaixz.bus.limiter.proxy.ByteBuddyProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 扫描注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimiterScanner implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = Builder.getUserClass(bean.getClass());

        if (Provider.class.isAssignableFrom(clazz)) {
            StrategyManager.add((Provider) bean);
            return bean;
        }

        AtomicBoolean needProxy = new AtomicBoolean(false);
        Arrays.stream(clazz.getMethods()).forEach(method -> {
            Downgrade downgrade = searchAnnotation(method, Downgrade.class);
            if (ObjectKit.isNotNull(downgrade)) {
                MethodManager.addMethod(Builder.resolveMethodName(method),
                        new Pair<>(StrategyMode.FALLBACK, downgrade));
                needProxy.set(true);
            }

            Hotspot hotspot = searchAnnotation(method, Hotspot.class);
            if (ObjectKit.isNotNull(hotspot)) {
                MethodManager.addMethod(Builder.resolveMethodName(method),
                        new Pair<>(StrategyMode.HOT_METHOD, hotspot));
                needProxy.set(true);
            }

            Limiting limiting = searchAnnotation(method, Limiting.class);
            if (ObjectKit.isNotNull(limiting)) {
                MethodManager.addMethod(Builder.resolveMethodName(method),
                        new Pair<>(StrategyMode.REQUEST_LIMIT, limiting));
                needProxy.set(true);
            }

        });

        if (needProxy.get()) {
            try {
                ByteBuddyProxy buddy = new ByteBuddyProxy(bean, clazz);
                return buddy.proxy();
            } catch (Exception e) {
                throw new BeanInitializationException(e.getMessage());
            }

        } else {
            return bean;
        }
    }

    private <A extends Annotation> A searchAnnotation(Method method, Class<A> annotationType) {
        A anno = AnnoKit.getAnnotation(method, annotationType);
        // 从接口层面向上搜索
        if (anno == null) {
            Class<?>[] ifaces = method.getDeclaringClass().getInterfaces();

            for (Class<?> ifaceClass : ifaces) {
                Method ifaceMethod = MethodKit.getMethod(ifaceClass, method.getName(), method.getParameterTypes());
                if (ifaceMethod != null) {
                    anno = searchAnnotation(ifaceMethod, annotationType);
                    break;
                }
            }
        }

        // 从父类逐级向上搜索
        if (anno == null) {
            Class<?> superClazz = method.getDeclaringClass().getSuperclass();
            if (superClazz != null) {
                Method superMethod = MethodKit.getMethod(superClazz, method.getName(), method.getParameterTypes());
                if (superMethod != null) {
                    return searchAnnotation(superMethod, annotationType);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return anno;
    }

}
