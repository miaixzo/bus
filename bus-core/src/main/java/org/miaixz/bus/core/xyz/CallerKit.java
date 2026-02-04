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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.caller.Caller;
import org.miaixz.bus.core.lang.caller.StackTraceCaller;

/**
 * Caller utility. This class provides methods to get the caller class at different stack depths and to check if a
 * method was invoked by a specific class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CallerKit {

    /**
     * Constructs a new CallerKit. Utility class constructor for static access.
     */
    private CallerKit() {
    }

    private static final Caller INSTANCE;

    static {
        INSTANCE = tryCreateCaller();
    }

    /**
     * Gets the caller class.
     *
     * @return The caller class.
     */
    public static Class<?> getCaller() {
        return INSTANCE.getCaller();
    }

    /**
     * Gets the caller of the caller.
     *
     * @return The caller of the caller.
     */
    public static Class<?> getCallers() {
        return INSTANCE.getCallers();
    }

    /**
     * Gets the caller class at a specified stack depth. The hierarchy is as follows:
     *
     * <pre>
     * 0: CallerKit
     * 1: The class that calls a method in CallerKit
     * 2: The caller of the class at depth 1
     * ...
     * </pre>
     *
     * @param depth The stack depth. 0 is this class, 1 is the class that called a method in this class, and so on.
     * @return The caller class at the specified depth.
     */
    public static Class<?> getCaller(final int depth) {
        return INSTANCE.getCaller(depth);
    }

    /**
     * Checks if the current method was invoked by a specific class.
     *
     * @param clazz The class to check against.
     * @return {@code true} if called by the specified class, {@code false} otherwise.
     */
    public static boolean isCalledBy(final Class<?> clazz) {
        return INSTANCE.isCalledBy(clazz);
    }

    /**
     * Gets the name of the method that called this method.
     *
     * @param isFullName If true, returns the fully qualified method name (including the class path).
     * @return The name of the calling method.
     */
    public static String getCallerMethodName(final boolean isFullName) {
        final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        final String methodName = stackTraceElement.getMethodName();
        if (!isFullName) {
            return methodName;
        }

        return stackTraceElement.getClassName() + Symbol.DOT + methodName;
    }

    /**
     * Tries to create a {@link Caller} implementation.
     *
     * @return A {@link Caller} implementation.
     */
    private static Caller tryCreateCaller() {
        return new StackTraceCaller();
    }

}
