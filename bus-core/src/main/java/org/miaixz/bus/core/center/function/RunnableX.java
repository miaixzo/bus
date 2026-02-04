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
package org.miaixz.bus.core.center.function;

import java.io.Serializable;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * A serializable {@link Runnable} interface that supports throwing exceptions and combining multiple runnables.
 *
 * @author Kimi Liu
 * @see Runnable
 * @since Java 17+
 */
@FunctionalInterface
public interface RunnableX extends Runnable, Serializable {

    /**
     * Combines multiple {@code RunnableX} instances to be executed in sequence.
     *
     * @param serRunnableArray An array of {@code RunnableX} instances to combine.
     * @return A combined {@code RunnableX} instance that executes the given runnables in order.
     */
    static RunnableX multi(final RunnableX... serRunnableArray) {
        return () -> Stream.of(serRunnableArray).forEach(RunnableX::run);
    }

    /**
     * Performs the runnable operation, potentially throwing an exception.
     *
     * @throws Throwable Any throwable exception that might occur during the operation.
     * @see Thread#run()
     */
    void running() throws Throwable;

    /**
     * Performs the runnable operation, automatically handling checked exceptions by wrapping them in a
     * {@link RuntimeException}.
     *
     * @throws RuntimeException A wrapped runtime exception if a checked exception occurs.
     * @see Thread#run()
     */
    @Override
    default void run() {
        try {
            running();
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}
