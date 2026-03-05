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
