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

import java.lang.reflect.Method;

/**
 * An implementation of {@link ProxyChain} that adapts a generic {@link Invocation}. This class acts as a bridge,
 * allowing different invocation contexts to be used within the proxy chain framework.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JoinPointInvoker implements ProxyChain {

    /**
     * The target object of the invocation.
     */
    private final Object target;

    /**
     * The underlying invocation context.
     */
    private final Invocation invocation;

    /**
     * Constructs a new JoinPointInvoker.
     *
     * @param target     The target object.
     * @param invocation The invocation context.
     */
    public JoinPointInvoker(Object target, Invocation invocation) {
        this.target = target;
        this.invocation = invocation;
    }

    @Override
    /**
     * Gets the proxy instance that the method was invoked on.
     *
     * @return The proxy instance.
     */
    public Object getProxy() {
        return invocation.getProxy();
    }

    @Override
    /**
     * Gets the {@link Method} object for the method that was invoked.
     *
     * @return The invoked method.
     */
    public Method getMethod() {
        return invocation.getMethod();
    }

    @Override
    /**
     * Gets the array of arguments that were passed to the method.
     *
     * @return The method arguments.
     */
    public Object[] getArguments() {
        return invocation.getArguments();
    }

    @Override
    /**
     * Proceeds with the invocation of the original method on the target object.
     *
     * @return The result of the method invocation.
     * @throws Throwable if the underlying method throws an exception.
     */
    public Object proceed() throws Throwable {
        return invocation.proceed();
    }

    @Override
    /**
     * Gets the names of the method parameters. Note: This implementation returns the argument values, not their names.
     *
     * @return An array of parameter values.
     */
    public Object[] getNames() {
        // This implementation returns the argument values, not their names.
        return getArguments();
    }

    @Override
    /**
     * Proceeds with the invocation, but with a new set of arguments.
     *
     * @param arguments The new arguments to use for the method invocation.
     * @return The result of the method invocation.
     * @throws Throwable if the underlying method throws an exception.
     */
    public Object proceed(Object[] args) throws Throwable {
        return invocation.getMethod().invoke(target, args);
    }

}
