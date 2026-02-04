/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
