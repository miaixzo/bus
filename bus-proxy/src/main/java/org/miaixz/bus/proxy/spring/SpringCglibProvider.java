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

import org.springframework.cglib.proxy.Enhancer;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.Provider;

/**
 * A proxy provider implementation that uses Spring's bundled CGLIB to create proxy objects. This allows for proxying
 * classes that do not implement interfaces by creating a subclass at runtime.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringCglibProvider implements Provider {

    /**
     * Constructs a new SpringCglibProvider instance.
     */
    public SpringCglibProvider() {
        // No initialization required.
    }

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
        Logger.debug(
                true,
                "Proxy",
                "CGLIB proxy constructor selection started: targetClass={}, constructorCount={}",
                targetClass.getName(),
                constructors.length);
        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final Object[] values = ClassKit.getDefaultValues(parameterTypes);

            try {
                T proxy = (T) enhancer.create(parameterTypes, values);
                Logger.debug(
                        false,
                        "Proxy",
                        "CGLIB proxy constructor selected: targetClass={}, constructorParameterCount={}, proxyClass={}",
                        targetClass.getName(),
                        parameterTypes.length,
                        proxy.getClass().getName());
                return proxy;
            } catch (final IllegalArgumentException e) {
                // Keep the last exception to throw if all constructors fail.
                finalException = e;
                Logger.warn(
                        false,
                        "Proxy",
                        e,
                        "CGLIB proxy constructor failed: targetClass={}, constructorParameterCount={}, exception={}",
                        targetClass.getName(),
                        parameterTypes.length,
                        e.getClass().getSimpleName());
            }
        }
        if (null != finalException) {
            throw finalException;
        }

        throw new IllegalArgumentException("No constructor could be used for creating a proxy for " + targetClass);
    }

    /**
     * Creates a proxy object for the given target, applying the specified aspect.
     *
     * @param <T>    The type of the target object.
     * @param target The object to be proxied.
     * @param aspect The aspect implementation containing the advice logic.
     * @return The proxied object.
     */

    @Override
    public <T> T proxy(final T target, final Aspect aspect) {
        final Class<?> targetClass = target.getClass();
        Logger.debug(
                true,
                "Proxy",
                "CGLIB proxy creation started: targetClass={}, aspectClass={}",
                targetClass.getName(),
                aspect == null ? null : aspect.getClass().getName());

        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new SpringCglibInterceptor(target, aspect));

        T proxy = create(enhancer, targetClass);
        Logger.debug(
                false,
                "Proxy",
                "CGLIB proxy creation completed: targetClass={}, proxyClass={}",
                targetClass.getName(),
                proxy == null ? null : proxy.getClass().getName());
        return proxy;
    }

}
