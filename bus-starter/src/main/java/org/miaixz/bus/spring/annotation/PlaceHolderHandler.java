/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring.annotation;

import org.miaixz.bus.core.xyz.MethodKit;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * An {@link InvocationHandler} that resolves annotation attribute values from the Spring {@link Environment} when the
 * annotation is accessed.
 * <p>
 * This handler is used by {@link AnnotationWrapper} to create dynamic proxies for annotations, allowing their
 * attributes to support placeholders (e.g., {@code ${property.name}}).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PlaceHolderHandler implements InvocationHandler {

    /**
     * The original annotation instance to delegate method calls to.
     */
    private final Annotation delegate;

    /**
     * The {@link PlaceHolderBinder} to use for resolving placeholders.
     */
    private final PlaceHolderBinder binder;

    /**
     * The Spring {@link Environment} for property resolution.
     */
    private final Environment environment;

    /**
     * Constructs a new {@code PlaceHolderHandler}.
     *
     * @param delegate    The original annotation instance to delegate method calls to.
     * @param binder      The {@link PlaceHolderBinder} to use for resolving placeholders.
     * @param environment The Spring {@link Environment} for property resolution.
     */
    public PlaceHolderHandler(Annotation delegate, PlaceHolderBinder binder, Environment environment) {
        this.delegate = delegate;
        this.binder = binder;
        this.environment = environment;
    }

    /**
     * Checks if the given method is one of the standard methods inherited from {@link Object}.
     *
     * @param method The method to check.
     * @return {@code true} if the method is an {@link Object} method (equals, hashCode, toString) or null,
     *         {@code false} otherwise.
     */
    public static boolean isObjectMethod(Method method) {
        return method != null && (method.getDeclaringClass() == Object.class || MethodKit.isEqualsMethod(method)
                || MethodKit.isHashCodeMethod(method) || MethodKit.isToStringMethod(method));
    }

    /**
     * Invokes a method on the proxied annotation, resolving placeholders in its return value if applicable.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@link Method} instance corresponding to the interface method invoked on the proxy instance.
     * @param args   An array of objects containing the values of the arguments passed in the method invocation.
     * @return The result of the method invocation, with placeholders resolved.
     * @throws Throwable if an exception occurs during method invocation or placeholder resolution.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(delegate, args);
        if (ret != null && !MethodKit.isObjectMethod(method) && MethodKit.isAttributeMethod(method)) {
            return resolvePlaceHolder(ret);
        }
        return ret;
    }

    /**
     * Recursively resolves placeholders in the given object.
     * <p>
     * If the object is an array, it resolves placeholders in each element. If it's a string, it uses the
     * {@link PlaceHolderBinder} to resolve placeholders. If it's another annotation (and not already a
     * {@link WrapperAnnotation}), it wraps it with {@link AnnotationWrapper}.
     * </p>
     *
     * @param origin The object whose placeholders need to be resolved.
     * @return The object with placeholders resolved.
     */
    public Object resolvePlaceHolder(Object origin) {
        if (origin.getClass().isArray()) {
            int length = Array.getLength(origin);
            Object ret = Array.newInstance(origin.getClass().getComponentType(), length);
            for (int i = 0; i < length; ++i) {
                Array.set(ret, i, resolvePlaceHolder(Array.get(origin, i)));
            }
            return ret;
        } else {
            return doResolvePlaceHolder(origin);
        }
    }

    /**
     * Resolves placeholders for a single object (non-array).
     *
     * @param origin The object to resolve placeholders in.
     * @return The object with placeholders resolved.
     */
    private Object doResolvePlaceHolder(Object origin) {
        if (origin instanceof String) {
            return binder.bind(environment, (String) origin);
        } else if (origin instanceof Annotation && !(origin instanceof WrapperAnnotation)) {
            return AnnotationWrapper.of((Annotation) origin).withBinder(binder).withEnvironment(environment)
                    .wrap((Annotation) origin);
        } else {
            return origin;
        }
    }

}
