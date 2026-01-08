/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.proxy.spring;

import java.lang.reflect.Constructor;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.Provider;
import org.springframework.cglib.proxy.Enhancer;

/**
 * A proxy provider implementation that uses Spring's bundled CGLIB to create proxy objects. This allows for proxying
 * classes that do not implement interfaces by creating a subclass at runtime.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringCglibProvider implements Provider {

    /**
     * Creates a proxy instance using the provided {@link Enhancer}. This method attempts to find a suitable constructor
     * on the target class, iterating through them and trying to instantiate with default parameter values. This is
     * necessary for classes that do not have a default (no-argument) constructor.
     *
     * @param <T>         The type of the proxy object.
     * @param enhancer    The CGLIB {@link Enhancer}.
     * @param targetClass The class to be proxied.
     * @return The created proxy instance.
     * @throws IllegalArgumentException if no suitable constructor can be found.
     */
    private static <T> T create(final Enhancer enhancer, final Class<?> targetClass) {
        final Constructor<?>[] constructors = ReflectKit.getConstructors(targetClass);
        IllegalArgumentException finalException = null;
        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final Object[] values = ClassKit.getDefaultValues(parameterTypes);

            try {
                return (T) enhancer.create(parameterTypes, values);
            } catch (final IllegalArgumentException e) {
                // Keep the last exception to throw if all constructors fail.
                finalException = e;
            }
        }
        if (null != finalException) {
            throw finalException;
        }

        throw new IllegalArgumentException("No constructor could be used for creating a proxy for " + targetClass);
    }

    @Override
    /**
     * Creates a proxy object for the given target, applying the specified aspect.
     *
     * @param <T>    The type of the target object.
     * @param target The object to be proxied.
     * @param aspect The aspect implementation containing the advice logic.
     * @return The proxied object.
     */
    public <T> T proxy(final T target, final Aspect aspect) {
        final Class<?> targetClass = target.getClass();

        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new SpringCglibInterceptor(target, aspect));

        return create(enhancer, targetClass);
    }

}
