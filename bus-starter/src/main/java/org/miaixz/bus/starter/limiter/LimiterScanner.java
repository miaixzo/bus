/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * A Spring {@link InstantiationAwareBeanPostProcessor} that scans for rate limiting and circuit breaking annotations.
 * <p>
 * This processor inspects each bean after initialization. If the bean is a {@link Provider}, it is registered with the
 * {@link StrategyManager}. If the bean has methods annotated with {@link Limiting}, {@link Hotspot}, or
 * {@link Downgrade}, the bean is wrapped in a ByteBuddy proxy to enforce the specified strategies.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimiterScanner implements InstantiationAwareBeanPostProcessor {

    /**
     * Processes a bean after its initialization.
     * <p>
     * This method scans the bean's class and its methods for limiter-related annotations. If any such annotations are
     * found, it creates a proxy for the bean to apply the limiting/downgrading logic. It also registers any
     * {@link Provider} beans.
     * </p>
     *
     * @param bean     The bean instance to process.
     * @param beanName The name of the bean.
     * @return The original bean or a proxied version of the bean.
     * @throws BeansException if an error occurs during processing.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = Builder.getUserClass(bean.getClass());

        // Register custom strategy providers.
        if (Provider.class.isAssignableFrom(clazz)) {
            StrategyManager.add((Provider) bean);
            return bean;
        }

        AtomicBoolean needProxy = new AtomicBoolean(false);
        // Scan methods for annotations.
        Arrays.stream(clazz.getMethods()).forEach(method -> {
            Downgrade downgrade = searchAnnotation(method, Downgrade.class);
            if (ObjectKit.isNotNull(downgrade)) {
                MethodManager
                        .addMethod(Builder.resolveMethodName(method), new Pair<>(StrategyMode.FALLBACK, downgrade));
                needProxy.set(true);
            }

            Hotspot hotspot = searchAnnotation(method, Hotspot.class);
            if (ObjectKit.isNotNull(hotspot)) {
                MethodManager
                        .addMethod(Builder.resolveMethodName(method), new Pair<>(StrategyMode.HOT_METHOD, hotspot));
                needProxy.set(true);
            }

            Limiting limiting = searchAnnotation(method, Limiting.class);
            if (ObjectKit.isNotNull(limiting)) {
                MethodManager
                        .addMethod(Builder.resolveMethodName(method), new Pair<>(StrategyMode.REQUEST_LIMIT, limiting));
                needProxy.set(true);
            }
        });

        // If any annotations were found, create a proxy.
        if (needProxy.get()) {
            try {
                ByteBuddyProxy buddy = new ByteBuddyProxy(bean, clazz);
                return buddy.proxy();
            } catch (Exception e) {
                throw new BeanInitializationException("Failed to create limiter proxy for bean: " + beanName, e);
            }
        } else {
            return bean;
        }
    }

    /**
     * Recursively searches for an annotation on a method, its implemented interfaces, and its superclasses.
     *
     * @param method         The method to search.
     * @param annotationType The type of the annotation to find.
     * @param <A>            The annotation type.
     * @return The found annotation, or {@code null} if not found.
     */
    private <A extends Annotation> A searchAnnotation(Method method, Class<A> annotationType) {
        A anno = AnnoKit.getAnnotation(method, annotationType);

        // Search on interfaces.
        if (anno == null) {
            for (Class<?> ifaceClass : method.getDeclaringClass().getInterfaces()) {
                Method ifaceMethod = MethodKit.getMethod(ifaceClass, method.getName(), method.getParameterTypes());
                if (ifaceMethod != null) {
                    anno = searchAnnotation(ifaceMethod, annotationType);
                    if (anno != null) {
                        break;
                    }
                }
            }
        }

        // Search on superclasses.
        if (anno == null) {
            Class<?> superClazz = method.getDeclaringClass().getSuperclass();
            if (superClazz != null && !Object.class.equals(superClazz)) {
                Method superMethod = MethodKit.getMethod(superClazz, method.getName(), method.getParameterTypes());
                if (superMethod != null) {
                    return searchAnnotation(superMethod, annotationType);
                }
            }
        }

        return anno;
    }

}
