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
package org.miaixz.bus.core.lang.caller;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Implementation of the {@link Caller} interface that retrieves caller information by analyzing the current thread's
 * stack trace. This method is generally less efficient compared to other caller retrieval mechanisms and should be used
 * with caution in performance-critical applications.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StackTraceCaller implements Caller, Serializable {

    @Serial
    private static final long serialVersionUID = 2852251293153L;

    /**
     * The offset in the stack trace to account for internal method calls within this class and
     * {@link Thread#getStackTrace()}.
     */
    private static final int OFFSET = 2;

    /**
     * Retrieves the immediate calling class from the current thread's stack trace.
     *
     * @return The {@link Class} object representing the immediate caller, or {@code null} if not found.
     * @throws InternalException if the class of the caller cannot be found.
     */
    @Override
    public Class<?> getCaller() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (OFFSET + 1 >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + 1].getClassName();
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e, "[{}] not found!", className);
        }
    }

    /**
     * Retrieves the caller of the immediate caller from the current thread's stack trace.
     *
     * @return The {@link Class} object representing the caller's caller, or {@code null} if not found.
     * @throws InternalException if the class of the caller cannot be found.
     */
    @Override
    public Class<?> getCallers() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (OFFSET + 2 >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + 2].getClassName();
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e, "[{}] not found!", className);
        }
    }

    /**
     * Retrieves the calling class at a specific depth in the call stack. The depth is relative to the
     * {@link StackTraceCaller} class itself.
     *
     * <p>
     * Call stack depth explanation:
     * 
     * <pre>
     * 0: {@link Thread#getStackTrace()} itself (internal)
     * 1: {@link StackTraceCaller} method (internal)
     * 2: The class that calls a method within {@link StackTraceCaller}
     * 3: The caller of the class at depth 2
     * ... and so on.
     * </pre>
     *
     * @param depth The depth in the call stack, where 0 refers to the immediate caller of this method.
     * @return The {@link Class} object at the specified call stack depth, or {@code null} if the depth is out of
     *         bounds.
     * @throws InternalException if the class at the specified depth cannot be found.
     */
    @Override
    public Class<?> getCaller(final int depth) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (OFFSET + depth >= stackTrace.length) {
            return null;
        }
        final String className = stackTrace[OFFSET + depth].getClassName();
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e, "[{}] not found!", className);
        }
    }

    /**
     * Checks if the current method is called by a specific class by iterating through the stack trace.
     *
     * @param clazz The {@link Class} object to check against the call stack.
     * @return {@code true} if the given class is found in the current call stack, {@code false} otherwise.
     */
    @Override
    public boolean isCalledBy(final Class<?> clazz) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (final StackTraceElement element : stackTrace) {
            if (element.getClassName().equals(clazz.getName())) {
                return true;
            }
        }
        return false;
    }

}
