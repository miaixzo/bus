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
