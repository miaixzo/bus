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
package org.miaixz.bus.proxy.invoker;

import java.lang.reflect.Method;

/**
 * Represents an invocation of a method on a proxy instance. This is a central concept in AOP, providing access to the
 * method, its arguments, and a way to proceed with the original invocation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Invocation {

    /**
     * Gets the proxy instance that the method was invoked on.
     *
     * @return The proxy instance.
     */
    Object getProxy();

    /**
     * Gets the {@link Method} object for the method that was invoked.
     *
     * @return The invoked method.
     */
    Method getMethod();

    /**
     * Gets the array of arguments that were passed to the method.
     *
     * @return The method arguments.
     */
    Object[] getArguments();

    /**
     * Proceeds with the invocation of the original method on the target object.
     *
     * @return The result of the method invocation.
     * @throws Throwable if the underlying method throws an exception.
     */
    Object proceed() throws Throwable;

}
