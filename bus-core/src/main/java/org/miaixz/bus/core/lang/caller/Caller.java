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
package org.miaixz.bus.core.lang.caller;

import org.miaixz.bus.core.xyz.CallerKit;

/**
 * Interface for obtaining caller information. Implementations of this interface provide methods to retrieve the calling
 * class, multi-level callers, and check if a specific class is in the call stack.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Caller {

    /**
     * Retrieves the immediate calling class.
     *
     * @return The {@link Class} object representing the immediate caller.
     */
    Class<?> getCaller();

    /**
     * Retrieves the caller of the immediate caller. This typically represents the class that invoked the method
     * containing the call to {@code getCaller()}.
     *
     * @return The {@link Class} object representing the caller's caller.
     */
    Class<?> getCallers();

    /**
     * Retrieves the calling class at a specific depth in the call stack. The depth is relative to the {@link CallerKit}
     * class itself.
     *
     * <p>
     * Call stack depth explanation:
     * 
     * <pre>
     * 0: {@link CallerKit} itself
     * 1: The class that calls a method within {@link CallerKit}
     * 2: The caller of the class at depth 1
     * ... and so on.
     * </pre>
     *
     * @param depth The depth in the call stack. 0 for {@link CallerKit} itself, 1 for the class calling
     *              {@link CallerKit}, 2 for the caller's caller, and so forth.
     * @return The {@link Class} object at the specified call stack depth.
     */
    Class<?> getCaller(int depth);

    /**
     * Checks if the current method is called by a specific class.
     *
     * @param clazz The {@link Class} object to check against the call stack.
     * @return {@code true} if the current method is called by the specified class, {@code false} otherwise.
     */
    boolean isCalledBy(Class<?> clazz);

}
