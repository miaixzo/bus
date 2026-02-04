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
