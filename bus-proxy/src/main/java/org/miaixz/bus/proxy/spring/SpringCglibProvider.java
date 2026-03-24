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
 * @since Java 21+
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
