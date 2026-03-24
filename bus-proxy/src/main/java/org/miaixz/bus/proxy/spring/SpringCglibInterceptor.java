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

import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.invoker.Interceptor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A dynamic proxy aspect implementation using Spring's bundled CGLIB {@link MethodInterceptor}. This class intercepts
 * method calls on a CGLIB proxy, allowing an {@link Aspect} to be applied.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringCglibInterceptor extends Interceptor implements MethodInterceptor {

    @Serial
    private static final long serialVersionUID = 2852260139720L;

    /**
     * Constructs a new {@code SpringCglibInterceptor}.
     *
     * @param target The object to be proxied.
     * @param aspect The aspect implementation containing the advice logic.
     */
    public SpringCglibInterceptor(final Object target, final Aspect aspect) {
        super(target, aspect);
    }

    /**
     * Intercepts a method invocation on the CGLIB proxy instance. It applies the before, after, and after-throwing
     * advice from the configured {@link Aspect} around the actual method call on the target object.
     *
     * @param object The proxy instance.
     * @param method The intercepted method.
     * @param args   The method arguments.
     * @param proxy  The CGLIB proxy used to invoke the original method.
     * @return The result of the method invocation.
     * @throws Throwable if the underlying method throws an exception.
     */
    @Override
    public Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy proxy)
            throws Throwable {
        final Object target = this.target;
        Object result = null;

        // "Before" advice
        if (aspect.before(target, method, args)) {
            try {
                result = proxy.invoke(target, args);
            } catch (final Throwable e) {
                Throwable throwable = e;
                if (throwable instanceof InvocationTargetException) {
                    throwable = ((InvocationTargetException) throwable).getTargetException();
                }

                // "After-throwing" advice
                if (aspect.afterException(target, method, args, throwable)) {
                    throw throwable;
                }
            }
        }

        // "After" advice
        if (aspect.after(target, method, args, result)) {
            return result;
        }
        return null;
    }

}
