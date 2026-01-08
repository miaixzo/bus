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
 * @since Java 17+
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
