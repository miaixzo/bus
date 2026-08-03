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
package org.miaixz.bus.spring.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.core.env.Environment;

import org.miaixz.bus.core.xyz.MethodKit;

/**
 * Resolves annotation attribute placeholders when a wrapped annotation is accessed.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PlaceholderHandler implements InvocationHandler {

    /**
     * Source annotation receiving delegated invocations.
     */
    private final Annotation delegate;
    /**
     * Binder used to resolve placeholder values.
     */
    private final PlaceholderBinder binder;
    /**
     * Environment supplying placeholder properties.
     */
    private final Environment environment;

    /**
     * Creates an annotation invocation handler.
     *
     * @param delegate    source annotation
     * @param binder      placeholder binder
     * @param environment environment used for resolution
     */
    public PlaceholderHandler(Annotation delegate, PlaceholderBinder binder, Environment environment) {
        this.delegate = delegate;
        this.binder = binder;
        this.environment = environment;
    }

    /**
     * Returns whether a method represents standard object behavior.
     *
     * @param method method to inspect
     * @return {@code true} for object methods
     */
    public static boolean isObjectMethod(Method method) {
        return method != null && (method.getDeclaringClass() == Object.class || MethodKit.isEqualsMethod(method)
                || MethodKit.isHashCodeMethod(method) || MethodKit.isToStringMethod(method));
    }

    /**
     * Invokes an annotation method and resolves placeholders in its result.
     *
     * @param proxy  annotation proxy
     * @param method invoked method
     * @param args   invocation arguments
     * @return resolved method result
     * @throws Throwable when delegation or resolution fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(this.delegate, args);
        if (result != null && !MethodKit.isObjectMethod(method) && MethodKit.isAttributeMethod(method)) {
            return resolvePlaceholder(result);
        }
        return result;
    }

    /**
     * Recursively resolves placeholders in annotation attribute values.
     *
     * @param origin source attribute value
     * @return resolved attribute value
     */
    public Object resolvePlaceholder(Object origin) {
        if (origin.getClass().isArray()) {
            int length = Array.getLength(origin);
            Object result = Array.newInstance(origin.getClass().getComponentType(), length);
            for (int i = 0; i < length; ++i) {
                Array.set(result, i, resolvePlaceholder(Array.get(origin, i)));
            }
            return result;
        }
        return doResolvePlaceholder(origin);
    }

    /**
     * Resolves one non-array annotation attribute value.
     *
     * @param origin source attribute value
     * @return resolved attribute value
     */
    private Object doResolvePlaceholder(Object origin) {
        if (origin instanceof String string) {
            return this.binder.bind(this.environment, string);
        }
        if (origin instanceof Annotation annotation && !(origin instanceof WrapperAnnotation)) {
            return AnnotationWrapper.of(annotation).withBinder(this.binder).withEnvironment(this.environment)
                    .wrap(annotation);
        }
        return origin;
    }

}
