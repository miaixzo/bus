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
