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
package org.miaixz.bus.proxy;

import java.lang.reflect.Method;

/**
 * An interface representing an aspect in Aspect-Oriented Programming (AOP). An aspect allows for executing custom logic
 * (advice) before, after, and upon exception of a method invocation on a target object.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Aspect {

    /**
     * The "before" advice, executed before the target method is invoked.
     *
     * @param target The target object.
     * @param method The method being invoked.
     * @param args   The arguments passed to the method.
     * @return {@code true} to proceed with the method invocation, {@code false} to block it.
     */
    boolean before(Object target, Method method, Object[] args);

    /**
     * The "after" advice, executed after the target method returns successfully.
     * <p>
     * This advice will not be executed if the target method throws an exception and
     * {@link #afterException(Object, Method, Object[], Throwable)} returns {@code true} (suppressing the exception).
     *
     * @param target    The target object.
     * @param method    The method that was invoked.
     * @param args      The arguments that were passed to the method.
     * @param returnVal The value returned by the target method.
     * @return {@code true} to allow the original return value to be returned, {@code false} to block it (may result in
     *         a null return).
     */
    boolean after(Object target, Method method, Object[] args, Object returnVal);

    /**
     * The "after-throwing" advice, executed when the target method throws an exception.
     *
     * @param target The target object.
     * @param method The method that was invoked.
     * @param args   The arguments that were passed to the method.
     * @param e      The exception thrown by the target method.
     * @return {@code true} to suppress the exception (the proxy will not rethrow it), {@code false} to allow it to be
     *         rethrown.
     */
    boolean afterException(Object target, Method method, Object[] args, Throwable e);

}
