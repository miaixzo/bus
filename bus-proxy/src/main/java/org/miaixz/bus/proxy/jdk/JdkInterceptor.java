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
package org.miaixz.bus.proxy.jdk;

import org.miaixz.bus.core.xyz.ModifierKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.invoker.Interceptor;

import java.io.Serial;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A dynamic proxy aspect implementation using the JDK's {@link InvocationHandler}. This class intercepts method calls
 * on a proxy, allowing an {@link Aspect} to be applied.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkInterceptor extends Interceptor implements InvocationHandler {

    @Serial
    private static final long serialVersionUID = 2852259927076L;

    /**
     * Constructs a new {@code JdkInterceptor}.
     *
     * @param target The object to be proxied.
     * @param aspect The aspect implementation containing the advice logic.
     */
    public JdkInterceptor(final Object target, final Aspect aspect) {
        super(target, aspect);
    }

    /**
     * Intercepts a method invocation on the proxy instance. It applies the before, after, and after-throwing advice
     * from the configured {@link Aspect} around the actual method call on the target object.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@code Method} instance corresponding to the interface method invoked on the proxy instance.
     * @param args   An array of objects containing the values of the arguments passed in the method invocation on the
     *               proxy instance.
     * @return The value to return from the method invocation on the proxy instance.
     * @throws Throwable The exception to throw from the method invocation on the proxy instance.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object target = this.target;
        final Aspect aspect = this.aspect;
        Object result = null;

        // "Before" advice
        if (aspect.before(target, method, args)) {
            ReflectKit.setAccessible(method);

            try {
                result = method.invoke(ModifierKit.isStatic(method) ? null : target, args);
            } catch (final InvocationTargetException e) {
                // "After-throwing" advice (only catches exceptions from the business logic)
                if (aspect.afterException(target, method, args, e.getTargetException())) {
                    // Rethrow if the aspect allows it
                    throw e;
                }
            }

            // "After" advice
            if (aspect.after(target, method, args, result)) {
                return result;
            }
        }
        return null;
    }

}
