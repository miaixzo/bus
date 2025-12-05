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
package org.miaixz.bus.core;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * A generic handler interface that defines callback methods for pre- and post-task execution, as well as logic for
 * property-based configuration.
 *
 * @param <T> The type of object this handler deals with, though it is not directly used in the default methods.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler<T> extends Order, Serializable {

    /**
     * A pre-processing callback method that is invoked before the main task is executed. This can be used for
     * initialization, validation, or pre-processing operations.
     *
     * @param executor The executor, which may be a proxy object.
     * @param args     Variable arguments that can pass additional context or data.
     * @return {@code true} to proceed with task execution, or {@code false} to interrupt it.
     */
    default boolean before(Executor executor, Object... args) {
        // do nothing
        return true;
    }

    /**
     * A post-processing callback method that is invoked after the main task has completed. This can be used for
     * resource cleanup, logging, or post-processing operations.
     *
     * @param executor The executor, which may be a proxy object.
     * @param args     Variable arguments that can pass additional context or data.
     * @return {@code true} if the post-processing was successful, or {@code false} if it failed.
     */
    default boolean after(Executor executor, Object... args) {
        // do nothing
        return true;
    }

    /**
     * Sets the properties for this handler, allowing for external configuration. This method can be used to configure
     * the handler's attributes or parameters.
     *
     * @param properties A {@link Properties} object containing configuration key-value pairs.
     * @return {@code true} if the properties were set successfully, or {@code false} on failure.
     */
    default boolean setProperties(Properties properties) {
        // do nothing
        return true;
    }

}
