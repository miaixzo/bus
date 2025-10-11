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
package org.miaixz.bus.proxy.invoker;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.lang.reflect.Method;

/**
 * An implementation of {@link ProxyChain} that adapts an AspectJ {@link ProceedingJoinPoint}. This allows AspectJ join
 * points to be used within this proxy framework.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InvocationInvoker implements ProxyChain {

    /** The underlying AspectJ join point. */
    private final ProceedingJoinPoint joinPoint;
    /** The lazily initialized method from the join point. */
    private Method method;

    /**
     * Constructs a new InvocationInvoker.
     *
     * @param joinPoint The AspectJ {@link ProceedingJoinPoint} to wrap.
     */
    public InvocationInvoker(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    /**
     * Gets the names of the parameters of the intercepted method.
     *
     * @return An array of parameter names.
     */
    @Override
    public Object[] getNames() {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getParameterNames();
    }

    /**
     * Gets the arguments passed to the intercepted method.
     *
     * @return An array of arguments.
     */
    @Override
    public Object[] getArguments() {
        return joinPoint.getArgs();
    }

    /**
     * Gets the target instance being intercepted.
     *
     * @return The target instance.
     */
    @Override
    public Object getProxy() {
        return joinPoint.getTarget();
    }

    /**
     * Gets the intercepted method.
     *
     * @return The {@link Method} being invoked.
     */
    @Override
    public Method getMethod() {
        if (ObjectKit.isEmpty(method)) {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            this.method = methodSignature.getMethod();
        }
        return method;
    }

    /**
     * Proceeds with the original method execution.
     *
     * @return The result of the method execution.
     * @throws Throwable if the underlying method throws an exception.
     */
    @Override
    public Object proceed() throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Proceeds with the original method execution, but with a new set of arguments.
     *
     * @param arguments The new arguments to use for the invocation.
     * @return The result of the method execution.
     * @throws Throwable if the underlying method throws an exception.
     */
    @Override
    public Object proceed(Object[] arguments) throws Throwable {
        return joinPoint.proceed(arguments);
    }

}
